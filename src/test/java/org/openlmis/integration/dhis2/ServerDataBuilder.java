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

package org.openlmis.integration.dhis2;

import java.util.UUID;
import org.openlmis.integration.dhis2.domain.server.Server;

public class ServerDataBuilder {

  private UUID id = UUID.randomUUID();
  private String name = "name";
  private String url = "http://test.configuration";
  private String username = "username";
  private String password = "$2a$12$/MRrjNIDYgba/9K6i.zNAOSMJFkWWwJHVYXGp/s3OfSbWL1fsiMWG";

  public ServerDataBuilder withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Builds new instance of Server (with id field).
   */
  public Server build() {
    Server server = buildAsNew();
    server.setId(id);

    return server;
  }

  /**
   * Builds new instance of Server as a new object (without id field).
   */
  public Server buildAsNew() {
    return new Server(name, url, username, password);
  }

}
