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

package org.openlmis.integration.dhis2.builder;

import java.util.UUID;
import org.openlmis.integration.dhis2.domain.facility.SharedFacility;
import org.openlmis.integration.dhis2.domain.server.Server;

public class SharedFacilityDataBuilder {

  private static final UUID ID = UUID.randomUUID();
  private static final String CODE = "test-code";
  private static final UUID FACILITY_ID = UUID.randomUUID();
  private static final UUID ORG_UNIT_ID = UUID.randomUUID();

  private Server server = new ServerDataBuilder().build();

  public SharedFacilityDataBuilder withServer(Server server) {
    this.server = server;
    return this;
  }

  /**
   * Builds new instance of SharedFacility (with id field).
   */
  public SharedFacility build() {
    SharedFacility sharedFacility = buildAsNew();
    sharedFacility.setId(ID);
    return sharedFacility;
  }

  /**
   * Builds new instance of SharedFacility as a new object (without id field).
   */
  public SharedFacility buildAsNew() {
    return new SharedFacility(CODE, FACILITY_ID, ORG_UNIT_ID, server);
  }

}
