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

package org.openlmis.integration.dhis2.domain.periodmapping;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.integration.dhis2.builder.DatasetDataBuilder;
import org.openlmis.integration.dhis2.builder.PeriodMappingDataBuilder;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.dto.periodmapping.PeriodMappingDto;

public class PeriodMappingTest {

  @Test
  public void equalsContract() {
    Dataset ds1 = new DatasetDataBuilder().build();
    Dataset ds2 = new Dataset();

    EqualsVerifier
            .forClass(PeriodMapping.class)
            .withRedefinedSuperclass()
            .withPrefabValues(Dataset.class, ds1, ds2)
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
  }

  @Test
  public void shouldUpdateFrom() {
    PeriodMapping periodMapping = new PeriodMappingDataBuilder().build();
    PeriodMappingDto dto = PeriodMappingDto.newInstance(periodMapping);
    dto.setName("test-name");

    periodMapping.updateFrom(dto);

    assertThat(periodMapping.getName()).isEqualTo("test-name");
  }

  @Test
  public void shouldExportData() {
    PeriodMapping periodMapping = new PeriodMappingDataBuilder().build();
    PeriodMappingDto dto = new PeriodMappingDto();

    periodMapping.export(dto);

    assertThat(dto.getId()).isEqualTo(periodMapping.getId());
    assertThat(dto.getName()).isEqualTo(periodMapping.getName());
  }

}