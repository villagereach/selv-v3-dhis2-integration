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
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.server.Server;

public class DatasetDataBuilder {
  private UUID id = UUID.randomUUID();
  private String name = "test-name";
  private String dhisDatasetId = "idXfoem";
  private String cronExpression = "0 5 * * *";
  private Server server = new ServerDataBuilder().build();

  public DatasetDataBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public DatasetDataBuilder withDatasetId(String datasetId) {
    this.dhisDatasetId = datasetId;
    return this;
  }

  public DatasetDataBuilder withCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
    return this;
  }

  public DatasetDataBuilder withServer(Server server) {
    this.server = server;
    return this;
  }

  /**
   * Builds new instance of Dataset (with id field).
   */
  public Dataset build() {
    Dataset dataset = buildAsNew();
    dataset.setId(id);

    return dataset;
  }

  /**
   * Builds new instance of Dataset as a new object (without id field).
   */
  public Dataset buildAsNew() {
    return new Dataset(name, dhisDatasetId, cronExpression, server);
  }

}
