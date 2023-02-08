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

package org.openlmis.integration.dhis2.web.execution;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.enumerator.DhisPeriod;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dhis.DataValue;
import org.openlmis.integration.dhis2.dto.dhis.DataValueSet;
import org.openlmis.integration.dhis2.dto.dhis.DhisDataset;
import org.openlmis.integration.dhis2.dto.dhis.OrganisationUnit;
import org.openlmis.integration.dhis2.dto.referencedata.MinimalFacilityDto;
import org.openlmis.integration.dhis2.repository.dataset.DatasetRepository;
import org.openlmis.integration.dhis2.repository.element.DataElementRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.openlmis.integration.dhis2.service.DhisDataService;
import org.openlmis.integration.dhis2.service.PeriodGenerator;
import org.openlmis.integration.dhis2.service.ReferenceDataService;
import org.openlmis.integration.dhis2.service.indicator.SimpleIndicatorService;
import org.openlmis.integration.dhis2.web.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller used to expose manual sync execution.
 */
@Controller
@RequestMapping(ExecutionController.RESOURCE_PATH)
@Transactional
public class ExecutionController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionController.class);

  public static final String RESOURCE_PATH = API_PATH + "/execute";

  @Autowired
  SimpleIndicatorService simpleIndicatorService;

  @Autowired
  private ServerRepository serverRepository;

  @Autowired
  private DatasetRepository datasetRepository;

  @Autowired
  private DataElementRepository dataElementRepository;

  @Autowired
  private DhisDataService dhisDataService;

  @Autowired
  private PeriodGenerator periodGenerator;

  @Autowired
  private ReferenceDataService referenceDataService;

  /**
   * Run manual execution.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public void runExecution() {
    LOGGER.debug("Running manual execution");
    final Long offset = 0L;

    List<Server> serverList = serverRepository.findAll();
    for (Server server: serverList) {
      List<Dataset> datasetList = datasetRepository.findAll();

      for (Dataset dataset: datasetList) {
        DataValueSet dataValueSet = new DataValueSet();

        dataValueSet.setDataSet(dataset.getDhisDatasetId());

        DhisPeriod periodEnumerator = DhisPeriod.valueOf(dataset.getCronExpression());
        Pair<ZonedDateTime, ZonedDateTime> periodRange =
                periodGenerator.generateRange(periodEnumerator, offset);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM");
        String formattedStartDate = periodRange.getFirst().format(dateTimeFormatter);

        dataValueSet.setPeriod(formattedStartDate);

        List<DataElement> dataElementList = dataElementRepository.findAll();

        DhisDataset dhisDataset = dhisDataService.getDhisDataSetById(dataset.getDhisDatasetId(),
                server.getUrl(), server.getUsername(), server.getPassword());

        List<String> dhisOrgUnitCodes = dhisDataset.getOrganisationUnits().stream()
                .map(OrganisationUnit::getCode).collect(Collectors.toList());

        List<String> refDataFacilityCodes = referenceDataService.findAllFacilities()
                .getContent().stream()
                .map(MinimalFacilityDto::getCode).collect(Collectors.toList());

        List<String> orgUnits = dhisOrgUnitCodes.stream()
                .filter(refDataFacilityCodes::contains)
                .collect(Collectors.toList());

        for (String orgUnit: orgUnits) {

          dataValueSet.setOrgUnit(orgUnit);

          List<DataValue> dataValues = new ArrayList<>();

          for (DataElement dataElement: dataElementList) {
            DataValue dataValue = new DataValue();
            String orderable = dataElement.getOrderable();

            dataValue.setDataElement(orderable);

            String openingBalance = simpleIndicatorService.generateOpeningBalance(
                    periodEnumerator, offset, orderable, orgUnit);

            dataValue.setValue(new BigDecimal(openingBalance));

            dataValues.add(dataValue);

            dataValueSet.setDataValues(dataValues);
          }

          dhisDataService.createDataValueSet(dataValueSet, server.getUrl(),
                  server.getUsername(), server.getPassword());
        }

      }

    }

  }

}