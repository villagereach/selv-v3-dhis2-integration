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

import java.time.LocalDate;
import java.util.UUID;
import org.openlmis.integration.dhis2.domain.periodmapping.PeriodMapping;
import org.openlmis.integration.dhis2.domain.server.Server;

public class PeriodMappingDataBuilder {

  private static final UUID ID = UUID.randomUUID();
  private static final String NAME = "test-name";
  private static final String SOURCE = "test-source";
  private static final String DHIS_PERIOD_ID = "test-dhis-period";
  private static final UUID PROCESSING_PERIOD_ID = UUID.randomUUID();
  private static final LocalDate START_DATE = LocalDate.of(2017, 1, 1);
  private static final LocalDate END_DATE = LocalDate.of(2017, 1, 31);

  private Server server = new ServerDataBuilder().build();

  public PeriodMappingDataBuilder withServer(Server server) {
    this.server = server;
    return this;
  }

  /**
   * Builds new instance of Period mapping (with id field).
   */
  public PeriodMapping build() {
    PeriodMapping periodMapping = buildAsNew();
    periodMapping.setId(ID);
    periodMapping.setServer(server);

    return periodMapping;
  }

  /**
   * Builds new instance of Period mapping as a new object (without id field).
   */
  public PeriodMapping buildAsNew() {
    PeriodMapping periodMapping = new PeriodMapping(NAME, SOURCE, DHIS_PERIOD_ID,
            PROCESSING_PERIOD_ID, START_DATE, END_DATE);
    periodMapping.setServer(server);
    return periodMapping;
  }

}
