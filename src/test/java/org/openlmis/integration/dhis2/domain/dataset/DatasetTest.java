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

package org.openlmis.integration.dhis2.domain.dataset;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.integration.dhis2.builder.DataElementDataBuilder;
import org.openlmis.integration.dhis2.builder.DatasetDataBuilder;
import org.openlmis.integration.dhis2.builder.ScheduleDataBuilder;
import org.openlmis.integration.dhis2.builder.ServerDataBuilder;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dataset.DatasetDto;


public class DatasetTest {

  @Test
  public void equalsContract() {
    Server sv1 = new ServerDataBuilder().build();
    Server sv2 = new Server();

    DataElement de1 = new DataElementDataBuilder().build();
    DataElement de2 = new DataElement();

    Schedule sc1 = new ScheduleDataBuilder().build();
    Schedule sc2 = new Schedule();

    EqualsVerifier
        .forClass(Dataset.class)
        .withRedefinedSuperclass()
        .withPrefabValues(Server.class, sv1, sv2)
        .withPrefabValues(DataElement.class, de1, de2)
        .withPrefabValues(Schedule.class, sc1, sc2)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldUpdateFrom() {
    Dataset dataset = new DatasetDataBuilder().build();
    DatasetDto dto = DatasetDto.newInstance(dataset);
    dto.setName("ala");

    dataset.updateFrom(dto);

    assertThat(dataset.getName()).isEqualTo("ala");
  }

  @Test
  public void shouldExportData() {
    Dataset dataset = new DatasetDataBuilder().build();
    DatasetDto dto = new DatasetDto();

    dataset.export(dto);

    assertThat(dto.getId()).isEqualTo(dataset.getId());
    assertThat(dto.getName()).isEqualTo(dataset.getName());
  }

}
