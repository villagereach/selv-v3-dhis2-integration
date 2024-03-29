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

package org.openlmis.integration.dhis2.domain.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.integration.dhis2.builder.DataElementDataBuilder;
import org.openlmis.integration.dhis2.builder.DatasetDataBuilder;
import org.openlmis.integration.dhis2.builder.ScheduleDataBuilder;
import org.openlmis.integration.dhis2.builder.ServerDataBuilder;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.schedule.ScheduleDto;

public class ScheduleTest {

  @Test
  public void equalsContract() {
    Server sv1 = new ServerDataBuilder().build();
    Server sv2 = new Server();

    Dataset ds1 = new DatasetDataBuilder().build();
    Dataset ds2 = new Dataset();

    DataElement de1 = new DataElementDataBuilder().build();
    DataElement de2 = new DataElement();

    EqualsVerifier
        .forClass(Schedule.class)
        .withRedefinedSuperclass()
        .withPrefabValues(Server.class, sv1, sv2)
        .withPrefabValues(Dataset.class, ds1, ds2)
        .withPrefabValues(DataElement.class, de1, de2)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldUpdateFrom() {
    Schedule schedule = new ScheduleDataBuilder().build();
    ScheduleDto dto = ScheduleDto.newInstance(schedule);
    dto.setTimeOffset(150);

    schedule.updateFrom(dto);

    assertThat(schedule.getTimeOffset()).isEqualTo(150);
  }

  @Test
  public void shouldExportData() {
    Schedule schedule = new ScheduleDataBuilder().build();
    ScheduleDto dto = new ScheduleDto();

    schedule.export(dto);

    assertThat(dto.getId()).isEqualTo(schedule.getId());
    assertThat(dto.getTimeOffset()).isEqualTo(schedule.getTimeOffset());
  }

}
