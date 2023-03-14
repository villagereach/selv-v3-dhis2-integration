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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.openlmis.integration.dhis2.domain.facility.SharedFacility;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dhis.OrganisationUnit;
import org.openlmis.integration.dhis2.dto.facility.SharedFacilityDto;
import org.openlmis.integration.dhis2.dto.referencedata.MinimalFacilityDto;
import org.openlmis.integration.dhis2.dto.server.ServerDto;
import org.openlmis.integration.dhis2.repository.facility.SharedFacilityRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.openlmis.integration.dhis2.service.DhisDataService;
import org.openlmis.integration.dhis2.service.ReferenceDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SharedFacilitySynchronizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SharedFacilitySynchronizer.class);

  @Autowired
  private ReferenceDataService referenceDataService;

  @Autowired
  private DhisDataService dhisDataService;

  @Autowired
  private ServerRepository serverRepository;

  @Autowired
  private SharedFacilityRepository sharedFacilityRepository;

  /**
   * Refreshes facilities between OpenLMIS and DHIS2. It deletes certain facilities if they are
   * no longer present in any of the services and adds new if a matching facility occurred
   */
  public void refreshSharedFacilities() {
    LOGGER.debug("Refreshing shared facilities");
    List<MinimalFacilityDto> refDataFacilities = referenceDataService
            .findAllFacilities().getContent();

    List<Server> servers = serverRepository.findAll();
    for (Server server: servers) {
      Set<SharedFacilityDto> allMatchingFacilities = new HashSet<>();
      Set<SharedFacilityDto> allNotMatchingFacilities = new HashSet<>();
      List<OrganisationUnit> organisationUnits = dhisDataService.getDhisOrgUnits(
              server.getUrl(), server.getUsername(), server.getPassword());

      for (OrganisationUnit orgUnit: organisationUnits) {
        String orgUnitCode = orgUnit.getCode();

        for (MinimalFacilityDto facilityDto : refDataFacilities) {
          String facilityCode = facilityDto.getCode();

          if (facilityCode.equals(orgUnitCode)) {
            allMatchingFacilities.add(new SharedFacilityDto(orgUnitCode, facilityDto.getId(),
                    UUID.fromString(orgUnit.getId()), ServerDto.newInstance(server)));
          } else {
            allNotMatchingFacilities.add((new SharedFacilityDto(orgUnitCode, facilityDto.getId(),
                    UUID.fromString(orgUnit.getId()), ServerDto.newInstance(server))));
          }

        }

      }

      // if previously added facilities are not matching then delete from db
      for (SharedFacilityDto notMatchingFacilityDto: allNotMatchingFacilities) {
        Optional<SharedFacility> sharedFacilityOptional =
                sharedFacilityRepository.findByCodeAndServerId(notMatchingFacilityDto.getCode(),
                        notMatchingFacilityDto.getServerDto().getId());
        sharedFacilityOptional.ifPresent(sharedFacility ->
                sharedFacilityRepository.delete(sharedFacility));
      }

      // if previously added facilities are matching then add them to db
      for (SharedFacilityDto matchingFacilityDto: allMatchingFacilities) {
        SharedFacility matchingFacility = SharedFacility.newInstance(matchingFacilityDto);
        Optional<SharedFacility> sharedFacilityOptional =
                sharedFacilityRepository.findByCodeAndServerId(matchingFacilityDto.getCode(),
                        matchingFacilityDto.getServerDto().getId());

        if (!sharedFacilityOptional.isPresent()) {
          sharedFacilityRepository.save(matchingFacility);
        }
      }
    }
  }

}
