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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import org.openlmis.integration.dhis2.domain.enumerator.DhisPeriod;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;
import org.openlmis.integration.dhis2.service.ProcessedDataExchangeService;
import org.openlmis.integration.dhis2.service.facility.SharedFacilitySynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class DynamicCronScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicCronScheduler.class);
  private static final String FACILITY_REFRESH_CRON = "0 20 * * * *";

  private Map<UUID, ScheduledFuture<?>> scheduledProcesses = new HashMap<>();

  @Autowired
  private TaskScheduler taskScheduler;

  @Autowired
  private ScheduleService scheduleService;

  @Autowired
  private PeriodGeneratorService periodGeneratorService;

  @Autowired
  private SharedFacilitySynchronizer sharedFacilitySynchronizer;

  @Autowired
  private ProcessedDataExchangeService processedDataExchangeService;

  @Scheduled(cron = FACILITY_REFRESH_CRON)  // every day at 8:00 PM UTC
  private void refreshSharedFacilities() {
    sharedFacilitySynchronizer.refreshSharedFacilities();
  }

  /**
   * Creates single new cron job.
   */
  public void createNewCron(Schedule schedule) {
    LOGGER.debug("Creating new cron job");
    DhisPeriod periodEnum = periodGeneratorService.fromString(schedule.getPeriodEnumerator());
    int offset = schedule.getTimeOffset();
    CronTrigger cronTrigger = new CronTrigger(periodEnum, offset);

    ScheduledFuture<?> newProcess = taskScheduler.schedule(
        () -> processedDataExchangeService.sendData(schedule), cronTrigger);
    scheduledProcesses.putIfAbsent(schedule.getId(), newProcess);
  }

  /**
   * Recreates cron jobs from database data after application startup.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void recreateTasks() {
    LOGGER.debug("Recreating cron jobs");
    List<Schedule> schedules = scheduleService.getAllSchedules();
    schedules.forEach(this::createNewCron);
  }

}
