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

package org.openlmis.integration.dhis2.service.schedule;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import org.openlmis.integration.dhis2.domain.enumerator.DhisPeriod;
import org.openlmis.integration.dhis2.domain.facility.SharedFacility;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dhis.DataValue;
import org.openlmis.integration.dhis2.dto.dhis.DataValueSet;
import org.openlmis.integration.dhis2.dto.dhis.OrganisationUnit;
import org.openlmis.integration.dhis2.dto.facility.SharedFacilityDto;
import org.openlmis.integration.dhis2.dto.referencedata.MinimalFacilityDto;
import org.openlmis.integration.dhis2.dto.server.ServerDto;
import org.openlmis.integration.dhis2.repository.facility.SharedFacilityRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.openlmis.integration.dhis2.service.DhisDataService;
import org.openlmis.integration.dhis2.service.ReferenceDataService;
import org.openlmis.integration.dhis2.service.facility.SharedFacilityService;
import org.openlmis.integration.dhis2.service.indicator.IndicatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class DynamicCronScheduler {

  private HashMap<UUID, ScheduledFuture<?>> scheduledProcesses = new HashMap<>();
  private List<SharedFacility> sharedFacilities;

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicCronScheduler.class);

  @Autowired
  private TaskScheduler taskScheduler;

  @Autowired
  private ReferenceDataService referenceDataService;

  @Autowired
  private ScheduleService scheduleService;

  @Autowired
  private PeriodGeneratorService periodGeneratorService;

  @Autowired
  private IndicatorService indicatorService;

  @Autowired
  private DhisDataService dhisDataService;

  @Autowired
  private SharedFacilityService sharedFacilityService;

  @Autowired
  private ServerRepository serverRepository;

  @Autowired
  private SharedFacilityRepository sharedFacilityRepository;

  /**
   * Refreshes facilities between OpenLMIS and DHIS2.
   */
  public void refreshOrgUnits() {
    LOGGER.debug("Refreshing organizational units");
    List<MinimalFacilityDto> refDataFacilities = referenceDataService
            .findAllFacilities().getContent();

    List<Server> servers = serverRepository.findAll();
    for (Server server: servers) {
      List<OrganisationUnit> organisationUnits = dhisDataService.getDhisOrgUnits(
              server.getUrl(), server.getUsername(), server.getPassword());

      for (OrganisationUnit orgUnit: organisationUnits) {
        final String orgUnitCode = orgUnit.getCode();
        Optional<SharedFacility> sharedFacility = sharedFacilityRepository.findByCode(orgUnitCode);
        Optional<MinimalFacilityDto> refDataFacility = refDataFacilities.stream()
                .filter(f -> f.getCode().equals(orgUnitCode)).findFirst();

        final boolean existInBothServices = refDataFacilities.stream()
                .anyMatch(f -> f.getCode().equals(orgUnitCode));
        final boolean existInSharedFacilities = sharedFacility.isPresent();

        if (existInSharedFacilities && !existInBothServices) {
          sharedFacilityService.deleteSharedFacility(sharedFacility.get().getId());
        } else if (!existInSharedFacilities && existInBothServices) {
          SharedFacilityDto sharedFacilityDto = new SharedFacilityDto(orgUnitCode,
                  refDataFacility.get().getId(), UUID.fromString(orgUnit.getId()),
                  ServerDto.newInstance(server));
          sharedFacilityService.createSharedFacility(sharedFacilityDto);
        }

      }

    }

  }

  /**
   * Sends data from OpenLMIS to DHIS2.
   */
  public void sendData(Schedule schedule) {
    final String orderable = schedule.getDataElement().getOrderable();
    final String sourceTable = schedule.getDataElement().getSource();
    final String indicator = schedule.getDataElement().getIndicator();

    final String dhisDatasetId = schedule.getDataset().getDhisDatasetId();
    final String periodEnum = schedule.getDataset().getCronExpression();
    final int timeOffset = schedule.getDataset().getTimeOffset();

    final Pair<ZonedDateTime, ZonedDateTime> periodRange = periodGeneratorService
            .generateRange(periodEnum, timeOffset);
    final String formattedStartDate = periodGeneratorService.formatDate(
            periodRange.getFirst(), periodEnum);

    final List<String> orgUnits = sharedFacilities.stream().map(SharedFacility::getCode)
            .collect(Collectors.toList());
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

      dhisDataService.createDataValueSet(dataValueSet, schedule.getServer().getUrl(),
              schedule.getServer().getUsername(), schedule.getServer().getPassword());
    }

  }

  /**
   * Creates single new cron job.
   */
  private void createNewCron(Schedule schedule) {
    LOGGER.debug("Creating new cron job");
    DhisPeriod periodEnum = periodGeneratorService.fromString(schedule.getPeriodEnumerator());
    int offset = schedule.getTimeOffset();
    CronTrigger cronTrigger = new CronTrigger(periodEnum, offset);

    ScheduledFuture<?> newProcess = taskScheduler.schedule(
        () -> sendData(schedule), cronTrigger);
    scheduledProcesses.putIfAbsent(schedule.getId(), newProcess);
  }

  /**
   * Recreates cron jobs from database data after application startup.
   */
  @EventListener(ApplicationReadyEvent.class)
  private void recreateTasks() {
    LOGGER.debug("Recreating cron jobs");
    sharedFacilities = sharedFacilityService.getAllSharedFacilities();
    List<Schedule> schedules = scheduleService.getAllSchedules();
    schedules.forEach(this::createNewCron);
  }

}
