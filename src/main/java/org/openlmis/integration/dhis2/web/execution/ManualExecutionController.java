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

import java.util.List;
import java.util.UUID;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;
import org.openlmis.integration.dhis2.service.communication.ProcessedDataExchangeService;
import org.openlmis.integration.dhis2.dto.facility.FacilityCodesWrapper;
import org.openlmis.integration.dhis2.service.ProcessedDataExchangeService;
import org.openlmis.integration.dhis2.service.facility.SharedFacilitySynchronizer;
import org.openlmis.integration.dhis2.service.role.PermissionService;
import org.openlmis.integration.dhis2.service.schedule.ScheduleService;
import org.openlmis.integration.dhis2.web.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller used to expose manual sync execution.
 */
@Controller
@RequestMapping(ManualExecutionController.RESOURCE_PATH)
public class ManualExecutionController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ManualExecutionController.class);

  public static final String RESOURCE_PATH = API_PATH + "/execute";

  @Autowired
  private ProcessedDataExchangeService processedDataExchangeService;

  @Autowired
  private SharedFacilitySynchronizer sharedFacilitySynchronizer;

  @Autowired
  private ScheduleService scheduleService;

  @Autowired
  private PermissionService permissionService;

  /**
   * Run manual execution.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public void runExecution() {
    permissionService.canManageDhisIntegration();
    LOGGER.debug("Running manual execution");
    sharedFacilitySynchronizer.refreshSharedFacilities();
    scheduleService.getAllSchedules().forEach(
        schedule -> processedDataExchangeService.sendData(schedule, null, null));
  }

  /**
   * Run manual execution for certain server, dataset, period mapping and facilities.
   */
  @PostMapping(params = {"serverId", "datasetId", "periodMappingId"})
  @ResponseStatus(HttpStatus.OK)
  public void runExecution(@RequestParam(value = "serverId") UUID serverId,
                           @RequestParam(value = "datasetId") UUID datasetId,
                           @RequestParam(value = "periodMappingId") UUID periodMappingId,
                           @RequestBody(required = false) FacilityCodesWrapper facilityCodes) {
    permissionService.canManageDhisIntegration();
    LOGGER.debug("Running manual execution");
    sharedFacilitySynchronizer.refreshSharedFacilities();

    List<Schedule> schedules = scheduleService
        .getSchedulesByServerAndDatasetId(serverId, datasetId);
    schedules.forEach(schedule -> processedDataExchangeService
            .sendData(schedule, periodMappingId, facilityCodes.getFacilityCodes()));
  }

}