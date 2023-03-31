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

package org.openlmis.integration.dhis2.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.facility.SharedFacility;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dhis.DataValue;
import org.openlmis.integration.dhis2.dto.dhis.DataValueSet;
import org.openlmis.integration.dhis2.dto.dhis.DhisResponseBody;
import org.openlmis.integration.dhis2.repository.facility.SharedFacilityRepository;
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

  @Autowired
  private PeriodGeneratorService periodGeneratorService;

  @Autowired
  private IndicatorService indicatorService;

  @Autowired
  private DhisDataService dhisDataService;

  @Autowired
  private SharedFacilityRepository sharedFacilityRepository;

  /**
   * Sends data from OpenLMIS to DHIS2.
   */
  public void sendData(Schedule schedule) {
    DataElement dataElement = schedule.getDataElement();
    final String orderable = dataElement.getOrderable();
    final String sourceTable = dataElement.getSource();
    final String indicator = dataElement.getIndicator();

    Dataset dataset = schedule.getDataset();
    final String dhisDatasetId = dataset.getDhisDatasetId();
    final String periodEnum = dataset.getCronExpression();
    final int timeOffset = dataset.getTimeOffset();

    Pair<ZonedDateTime, ZonedDateTime> periodRange;
    String formattedStartDate;
    if (sourceTable.equals("Requisition")) {
      periodRange = periodGeneratorService.getLastRequisitionPeriod();
      formattedStartDate = periodGeneratorService.formatDate(periodRange.getSecond(), "Monthly");
    } else {
      periodRange = periodGeneratorService.generateRange(periodEnum, timeOffset);
      formattedStartDate = periodGeneratorService.formatDate(periodRange.getFirst(), periodEnum);
    }

    final List<String> orgUnits = sharedFacilityRepository.findAll().stream()
            .map(SharedFacility::getCode).collect(Collectors.toList());
    for (String orgUnit: orgUnits) {
      final BigDecimal calculatedIndicator = indicatorService.generate(sourceTable,
              indicator, periodRange, orderable, orgUnit);

      DataValue dataValue = new DataValue();
      dataValue.setDataElement(orderable);
      dataValue.setValue(calculatedIndicator);

      DataValueSet dataValueSet = new DataValueSet();
      dataValueSet.setDataSet(dhisDatasetId);
      dataValueSet.setPeriod(formattedStartDate);
      dataValueSet.setOrgUnit(orgUnit);
      dataValueSet.setDataValues(Collections.singletonList(dataValue));

      Server server = schedule.getServer();
      DhisResponseBody dhisResponseBody = dhisDataService.createDataValueSet(dataValueSet,
              server.getUrl(), server.getUsername(), server.getPassword());
      LOGGER.debug("Sending data value set: " + dataValueSet);
      LOGGER.debug("DHIS2 response body: " + dhisResponseBody);
    }
  }

}
