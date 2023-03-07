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

package org.openlmis.integration.dhis2.repository;

import java.util.UUID;
import org.openlmis.integration.dhis2.builder.ServerDataBuilder;
import org.openlmis.integration.dhis2.builder.SharedFacilityDataBuilder;
import org.openlmis.integration.dhis2.domain.facility.SharedFacility;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.repository.facility.SharedFacilityRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class SharedFacilityRepositoryIntegrationTest extends
        BaseCrudRepositoryIntegrationTest<SharedFacility> {

  @Autowired
  private SharedFacilityRepository sharedFacilityRepository;

  @Autowired
  private ServerRepository serverRepository;

  @Override
  public CrudRepository<SharedFacility, UUID> getRepository() {
    return sharedFacilityRepository;
  }

  @Override
  public SharedFacility generateInstance() {
    Server server = new ServerDataBuilder().buildAsNew();
    serverRepository.save(server);

    return new SharedFacilityDataBuilder().withServer(server).buildAsNew();
  }

}
