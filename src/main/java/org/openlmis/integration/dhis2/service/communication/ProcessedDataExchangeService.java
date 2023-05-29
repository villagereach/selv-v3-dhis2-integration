/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.integration.dhis2.service.communication;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.facility.SharedFacility;
import org.openlmis.integration.dhis2.domain.periodmapping.PeriodMapping;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dhis.DataValue;
import org.openlmis.integration.dhis2.dto.dhis.DataValueSet;
import org.openlmis.integration.dhis2.dto.dhis.DhisPeriodType;
import org.openlmis.integration.dhis2.dto.dhis.DhisResponseBody;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.facility.SharedFacilityRepository;
import org.openlmis.integration.dhis2.repository.periodmapping.PeriodMappingRepository;
import org.openlmis.integration.dhis2.service.indicator.IndicatorService;
import org.openlmis.integration.dhis2.service.schedule.PeriodGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class ProcessedDataExchangeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessedDataExchangeService.class);
  private static final String DEFAULT_DHIS_PERIOD = "Monthly";

  @Autowired
  private PeriodGeneratorService periodGeneratorService;

  @Autowired
  private IndicatorService indicatorService;

  @Autowired
  private DhisDataService dhisDataService;

  @Autowired
  private SharedFacilityRepository sharedFacilityRepository;

  @Autowired
  private PeriodMappingRepository periodMappingRepository;

  /**
   * Sends data from OpenLMIS to DHIS2.
   *
   * @param schedule given {@link Schedule} object
   */
  public void sendData(Schedule schedule) {
    sendData(schedule, null, null);
  }

  /**
   * Sends data from OpenLMIS to DHIS2.
   *
   * @param schedule given {@link Schedule} object
   * @param periodMappingId id of specific {@link PeriodMapping}
   * @param facilityCodes codes of the facilities to be included in data transfer
   */
  public void sendData(Schedule schedule, UUID periodMappingId, List<String> facilityCodes) {
    DataElement dataElement = schedule.getDataElement();
    final String orderable = dataElement.getOrderable();
    final String categoryOptionCombo = dataElement.getCategoryCombo();
    final String sourceTable = dataElement.getSource();
    final String indicator = dataElement.getIndicator();

    Dataset dataset = schedule.getDataset();
    final String dhisDatasetId = dataset.getDhisDatasetId();
    final String periodEnum = dataset.getCronExpression();
    final int timeOffset = dataset.getTimeOffset();

    Server server = schedule.getServer();

    Pair<ZonedDateTime, ZonedDateTime> periodRange;
    String formattedStartDate;
    if (sourceTable.equals("Requisition")) {
      if (periodMappingId != null) {
        PeriodMapping periodMapping = getPeriodMapping(periodMappingId);
        periodRange = periodGeneratorService.generateRange(periodMapping);

        formattedStartDate = periodGeneratorService.formatDate(periodRange.getSecond(),
                getDhisPeriodTypeWithName(periodMapping.getDhisPeriod(), server));
      } else {
        periodRange = periodGeneratorService.getLastRequisitionPeriod();
        formattedStartDate = periodGeneratorService.formatDate(periodRange.getSecond(),
                DEFAULT_DHIS_PERIOD);
      }
    } else {
      periodRange = periodGeneratorService.generateRange(periodEnum, timeOffset);
      formattedStartDate = periodGeneratorService.formatDate(periodRange.getFirst(), periodEnum);
    }

    List<String> orgUnits = sharedFacilityRepository.findAll().stream()
            .map(SharedFacility::getCode)
            .collect(Collectors.toList());

    if (facilityCodes != null) {
      orgUnits = orgUnits.stream()
              .filter(facilityCodes::contains)
              .collect(Collectors.toList());
    }

    for (String orgUnit: orgUnits) {
      final BigDecimal calculatedIndicator = indicatorService.generate(sourceTable,
              indicator, periodRange, orderable, orgUnit);

      DataValue dataValue = buildDataValue(orderable, categoryOptionCombo, calculatedIndicator);
      DataValueSet dataValueSet = buildDataValueSet(dhisDatasetId, formattedStartDate, orgUnit,
              dataValue);
      DhisResponseBody dhisResponseBody = dhisDataService.sendDataValueSet(dataValueSet,
              server.getUrl(), server.getUsername(), server.getPassword());
      LOGGER.debug("Sending data value set: " + dataValueSet);
      LOGGER.debug("DHIS2 response body: " + dhisResponseBody);
    }
  }

  private PeriodMapping getPeriodMapping(UUID periodMappingId) {
    return periodMappingRepository
            .findById(periodMappingId)
            .orElseThrow(() -> new NotFoundException(
                    MessageKeys.ERROR_PERIOD_MAPPING_NOT_FOUND));
  }

  private DhisPeriodType getDhisPeriodTypeWithName(String name, Server server) {
    return dhisDataService
            .getDhisPeriodTypes(server.getUrl(), server.getUsername(), server.getPassword())
            .stream()
            .filter(pt -> name.equals(pt.getName()))
            .findAny()
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_PERIOD_TYPE_NOT_FOUND));
  }

  private DataValue buildDataValue(String dataElement, String categoryOptionCombo,
                                   BigDecimal value) {
    DataValue dataValue = new DataValue();
    dataValue.setDataElement(dataElement);
    dataValue.setCategoryOptionCombo(categoryOptionCombo);
    dataValue.setValue(value);
    return dataValue;
  }

  private DataValueSet buildDataValueSet(String dataset, String period, String orgUnit,
                                         DataValue dataValue) {
    DataValueSet dataValueSet = new DataValueSet();
    dataValueSet.setDataSet(dataset);
    dataValueSet.setPeriod(period);
    dataValueSet.setOrgUnit(orgUnit);
    dataValueSet.setDataValues(Collections.singletonList(dataValue));
    return dataValueSet;
  }

}
