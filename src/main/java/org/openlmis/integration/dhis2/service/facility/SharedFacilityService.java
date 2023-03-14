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

package org.openlmis.integration.dhis2.service.facility;

import java.util.List;
import java.util.UUID;
import org.openlmis.integration.dhis2.domain.facility.SharedFacility;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.facility.SharedFacilityDto;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.facility.SharedFacilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SharedFacilityService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SharedFacilityService.class);

  @Autowired
  private SharedFacilityRepository sharedFacilityRepository;

  /**
   * Retrieves the specified facility by id.
   */
  public SharedFacility getSharedFacility(UUID id) {
    return sharedFacilityRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_FACILITY_NOT_FOUND));
  }

  /**
   * Retrieves all facilities.
   */
  public List<SharedFacility> getAllSharedFacilities() {
    return sharedFacilityRepository.findAll();
  }

  /**
   * Allows the creation of a new facility.
   */
  public SharedFacility createSharedFacility(SharedFacilityDto sharedFacilityDto) {
    LOGGER.debug("Creating new shared facility");
    SharedFacility sharedFacility = new SharedFacility(
            sharedFacilityDto.getCode(), sharedFacilityDto.getFacilityId(),
            sharedFacilityDto.getOrgUnitId(), Server.newInstance(
                    sharedFacilityDto.getServerDto()));
    sharedFacility.setId(null);

    return sharedFacilityRepository.saveAndFlush(sharedFacility);
  }

  /**
   * Deletes the specified facility by id.
   */
  public void deleteSharedFacility(UUID id) {
    if (!sharedFacilityRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_FACILITY_NOT_FOUND);
    }

    sharedFacilityRepository.deleteById(id);
  }

}
