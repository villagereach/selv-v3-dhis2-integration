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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.integration.dhis2.domain.facility.SharedFacility;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dhis.OrganisationUnit;
import org.openlmis.integration.dhis2.dto.referencedata.MinimalFacilityDto;
import org.openlmis.integration.dhis2.dto.referencedata.PageDto;
import org.openlmis.integration.dhis2.repository.facility.SharedFacilityRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.openlmis.integration.dhis2.service.facility.SharedFacilitySynchronizer;
import org.springframework.data.domain.Sort;

@RunWith(MockitoJUnitRunner.class)
public class SharedFacilitySynchronizerTest {

  private static final String serverName = "test-server-name";
  private static final String serverUrl = "test-server-url";
  private static final String serverUsername = "test-server-username";
  private static final String serverPassword = "test-server-password";

  @Mock
  private ServerRepository serverRepository;

  @Mock
  private DhisDataService dhisDataService;

  @Mock
  private ReferenceDataService referenceDataService;

  @Mock
  private SharedFacilityRepository sharedFacilityRepository;

  @InjectMocks
  private SharedFacilitySynchronizer sharedFacilitySynchronizer;

  @Before
  public void setUp() throws Exception {
    UUID serverId = UUID.randomUUID();
    Server server = new Server(serverName, serverUrl, serverUsername,
            serverPassword);
    server.setId(serverId);
    when(serverRepository.findAll()).thenReturn(Collections.singletonList(server));
  }

  // facility code != org unit code, facility exist in shared
  @Test
  public void refreshOrgUnitsShouldDeleteNotMatchingFacilities() {
    final String matchingCode = "matching-code";
    final String notMatchingCode = "not-matching-code";
    final UUID facilityId = UUID.randomUUID();
    final UUID orgUnitId = UUID.randomUUID();

    MinimalFacilityDto facility = new MinimalFacilityDto();
    facility.setId(facilityId);
    facility.setCode(matchingCode);

    List<MinimalFacilityDto> facilityDto = Collections.singletonList(facility);
    PageDto<MinimalFacilityDto> facilityPageMock = createPageDto(facilityDto);
    when(referenceDataService.findAllFacilities()).thenReturn(facilityPageMock);

    OrganisationUnit orgUnit = new OrganisationUnit();
    orgUnit.setId(String.valueOf(orgUnitId));
    orgUnit.setCode(notMatchingCode);

    PageDto<OrganisationUnit> orgUnitsPage = createPageDto(Collections.singletonList(orgUnit));
    when(dhisDataService.getDhisOrgUnits(any(String.class), any(String.class),
            any(String.class))).thenReturn(orgUnitsPage);

    when(sharedFacilityRepository.findByCodeAndServerId(any(String.class), any(UUID.class)))
            .thenReturn(Optional.of(mock(SharedFacility.class)));

    sharedFacilitySynchronizer.refreshSharedFacilities();
    verify(sharedFacilityRepository).delete(any(SharedFacility.class));
  }

  // facility code = org unit code, facility does not exist in shared
  @Test
  public void refreshOrgUnitsShouldSaveMatchingFacilities() {
    final String matchingCode = "matching-code";
    final UUID facilityId = UUID.randomUUID();
    final UUID orgUnitId = UUID.randomUUID();

    MinimalFacilityDto facility = new MinimalFacilityDto();
    facility.setId(facilityId);
    facility.setCode(matchingCode);

    List<MinimalFacilityDto> facilityDto = Collections.singletonList(facility);
    PageDto<MinimalFacilityDto> facilityPageMock = createPageDto(facilityDto);
    when(referenceDataService.findAllFacilities()).thenReturn(facilityPageMock);

    OrganisationUnit orgUnit = new OrganisationUnit();
    orgUnit.setId(String.valueOf(orgUnitId));
    orgUnit.setCode(matchingCode);

    PageDto<OrganisationUnit> orgUnitsPage = createPageDto(Collections.singletonList(orgUnit));
    when(dhisDataService.getDhisOrgUnits(any(String.class), any(String.class),
            any(String.class))).thenReturn(orgUnitsPage);

    when(sharedFacilityRepository.findByCodeAndServerId(any(String.class), any(UUID.class)))
            .thenReturn(Optional.empty());

    sharedFacilitySynchronizer.refreshSharedFacilities();
    verify(sharedFacilityRepository).save(any(SharedFacility.class));
  }

  private <T> PageDto<T> createPageDto(List<T> content) {
    return new PageDto<>(false, false, 1, 1L, 1, 1,
            1, Sort.by("sort-order"), content);
  }

}