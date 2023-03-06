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

import java.util.List;
import java.util.UUID;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.element.DataElementDto;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.dataset.DatasetRepository;
import org.openlmis.integration.dhis2.repository.element.DataElementRepository;
import org.openlmis.integration.dhis2.repository.schedule.ScheduleRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleService.class);

  @Autowired
  private ServerRepository serverRepository;

  @Autowired
  private DatasetRepository datasetRepository;

  @Autowired
  private DataElementRepository dataElementRepository;

  @Autowired
  private ScheduleRepository scheduleRepository;

  /**
   * Retrieves the specified schedule.
   */
  public Schedule getSchedule(UUID id) {
    return scheduleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SCHEDULE_NOT_FOUND));
  }

  /**
   * Retrieves all schedules.
   */
  public List<Schedule> getAllSchedules() {
    return scheduleRepository.findAll();
  }

  /**
   * Allows the creation of a new schedule.
   */
  public Schedule createSchedule(DataElementDto dataElementDto) {
    LOGGER.debug("Creating new scheduler");

    Server server = serverRepository.findById(
            dataElementDto.getDatasetDto().getServerDto().getId())
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND));

    Dataset dataset = datasetRepository.findById(dataElementDto.getDatasetDto().getId())
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_DATASET_NOT_FOUND));

    DataElement dataElement = dataElementRepository.findById(dataElementDto.getId())
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_DATAELEMENT_NOT_FOUND));

    Schedule newSchedule = new Schedule(dataset.getCronExpression(), dataset.getTimeOffset(),
            server, dataset, dataElement);
    newSchedule.setId(null);
    newSchedule = scheduleRepository.saveAndFlush(newSchedule);

    return newSchedule;
  }

  /**
   * Deletes the specified schedule.
   */
  public void deleteSchedule(UUID id) {
    if (!scheduleRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_SCHEDULE_NOT_FOUND);
    }

    scheduleRepository.deleteById(id);
  }

}
