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

package org.openlmis.integration.dhis2.domain.facility;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.openlmis.integration.dhis2.ToStringTestUtils;
import org.openlmis.integration.dhis2.builder.ServerDataBuilder;
import org.openlmis.integration.dhis2.builder.SharedFacilityDataBuilder;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.facility.SharedFacilityDto;

public class SharedFacilityTest {

  @Test
  public void equalsContract() {
    Server sv1 = new ServerDataBuilder().build();
    Server sv2 = new Server();

    EqualsVerifier
        .forClass(SharedFacility.class)
        .withRedefinedSuperclass()
        .withPrefabValues(Server.class, sv1, sv2)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    SharedFacility sharedFacility = new SharedFacilityDataBuilder().build();
    ToStringTestUtils.verify(SharedFacility.class, sharedFacility, "TEXT");
  }

  @Test
  public void shouldUpdateFrom() {
    SharedFacility sharedFacility = new SharedFacilityDataBuilder().build();
    SharedFacilityDto dto = SharedFacilityDto.newInstance(sharedFacility);
    dto.setCode("code");

    sharedFacility.updateFrom(dto);

    assertThat(sharedFacility.getCode()).isEqualTo("code");
  }

  @Test
  public void shouldExportData() {
    SharedFacility sharedFacility = new SharedFacilityDataBuilder().build();
    SharedFacilityDto dto = new SharedFacilityDto();

    sharedFacility.export(dto);

    Assertions.assertThat(dto.getId()).isEqualTo(sharedFacility.getId());
    assertThat(dto.getCode()).isEqualTo(sharedFacility.getCode());
  }

}
