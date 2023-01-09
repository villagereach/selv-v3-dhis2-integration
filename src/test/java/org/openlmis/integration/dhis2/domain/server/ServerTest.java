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

package org.openlmis.integration.dhis2.domain.server;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.openlmis.integration.dhis2.DatasetDataBuilder;
import org.openlmis.integration.dhis2.ServerDataBuilder;
import org.openlmis.integration.dhis2.ToStringTestUtils;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.dto.server.ServerDto;

public class ServerTest {

  @Test
  public void equalsContract() {
    Dataset ds1 = new DatasetDataBuilder().build();
    Dataset ds2 = new Dataset();

    EqualsVerifier
        .forClass(Server.class)
        .withRedefinedSuperclass()
        .withPrefabValues(Dataset.class, ds1, ds2)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    Server server = new ServerDataBuilder().build();
    ToStringTestUtils.verify(Server.class, server, "TEXT");
  }

  @Test
  public void shouldUpdateFrom() {
    Server server = new ServerDataBuilder().build();
    ServerDto dto = ServerDto.newInstance(server);
    dto.setName("ala");

    server.updateFrom(dto);

    assertThat(server.getName()).isEqualTo("ala");
  }

  @Test
  public void shouldExportData() {
    Server server = new ServerDataBuilder().build();
    ServerDto dto = new ServerDto();

    server.export(dto);

    Assertions.assertThat(dto.getId()).isEqualTo(server.getId());
    assertThat(dto.getName()).isEqualTo(server.getName());
  }

}
