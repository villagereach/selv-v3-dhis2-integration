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

package org.openlmis.integration.dhis2.domain.element;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.openlmis.integration.dhis2.DataElementDataBuilder;
import org.openlmis.integration.dhis2.DatasetDataBuilder;
import org.openlmis.integration.dhis2.ToStringTestUtils;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.dto.element.DataElementDto;

public class DataElementTest {

  @Test
  public void equalsContract() {
    Dataset ds1 = new DatasetDataBuilder().build();
    Dataset ds2 = new Dataset();

    EqualsVerifier
        .forClass(DataElement.class)
        .withRedefinedSuperclass()
        .withPrefabValues(Dataset.class, ds1, ds2)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    DataElement dataElement = new DataElementDataBuilder().build();
    ToStringTestUtils.verify(DataElement.class, dataElement, "TEXT");
  }

  @Test
  public void shouldUpdateFrom() {
    DataElement dataElement = new DataElementDataBuilder().build();
    DataElementDto dto = DataElementDto.newInstance(dataElement);
    dto.setName("ala");

    dataElement.updateFrom(dto);

    assertThat(dataElement.getName()).isEqualTo("ala");
  }

  @Test
  public void shouldExportData() {
    DataElement dataElement = new DataElementDataBuilder().build();
    DataElementDto dto = new DataElementDto();

    dataElement.export(dto);

    Assertions.assertThat(dto.getId()).isEqualTo(dataElement.getId());
    assertThat(dto.getName()).isEqualTo(dataElement.getName());
  }

}
