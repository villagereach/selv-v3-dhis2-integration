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

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.server.Server;

public class ServerDataBuilder {

  private static final UUID ID = UUID.randomUUID();
  private static final String NAME = "test-name";
  private static final String URL = "http://test.configuration";
  private static final String USERNAME = "test-username";
  private static final String PASSWORD = "test-password";

  private List<Dataset> datasets = Collections.emptyList();

  public ServerDataBuilder withDatasets(List<Dataset> datasets) {
    this.datasets = datasets;
    return this;
  }

  /**
   * Builds new instance of Server (with id field).
   */
  public Server build() {
    Server server = buildAsNew();
    server.setId(ID);
    return server;
  }

  /**
   * Builds new instance of Server as a new object (without id field).
   */
  public Server buildAsNew() {
    return new Server(NAME, URL, USERNAME, PASSWORD, datasets);
  }

}