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
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;
import org.openlmis.integration.dhis2.domain.server.Server;

public class ScheduleDataBuilder {

  private static final UUID ID = UUID.randomUUID();
  private static final String PERIOD_ENUMERATOR = "test-enumerator";
  private static final int TIME_OFFSET = 150;

  private Server server = new ServerDataBuilder().build();
  private Dataset dataset = new DatasetDataBuilder().build();
  private DataElement dataElement = new DataElementDataBuilder().build();

  public ScheduleDataBuilder withServer(Server server) {
    this.server = server;
    return this;
  }

  public ScheduleDataBuilder withDataset(Dataset dataset) {
    this.dataset = dataset;
    return this;
  }

  public ScheduleDataBuilder withElement(DataElement dataElement) {
    this.dataElement = dataElement;
    return this;
  }

  /**
   * Builds new instance of Schedule (with id field).
   */
  public Schedule build() {
    Schedule schedule = buildAsNew();
    schedule.setId(ID);
    return schedule;
  }

  /**
   * Builds new instance of Schedule as a new object (without id field).
   */
  public Schedule buildAsNew() {
    return new Schedule(PERIOD_ENUMERATOR, TIME_OFFSET, server, dataset, dataElement);
  }

}
