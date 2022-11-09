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

package org.openlmis.integration.dhis2.domain;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.openlmis.integration.dhis2.ToStringTestUtils;
import org.openlmis.integration.dhis2.WidgetDataBuilder;
import org.openlmis.integration.dhis2.web.widget.WidgetDto;

public class WidgetTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(Widget.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    Widget widget = new WidgetDataBuilder().build();
    ToStringTestUtils.verify(Widget.class, widget, "TEXT");
  }

  @Test
  public void shouldCreateNewInstance() {
    Widget widget = new WidgetDataBuilder().build();
    WidgetDto dto = WidgetDto.newInstance(widget);

    Widget newWidget = Widget.newInstance(dto);

    assertThat(newWidget).isEqualTo(widget);
  }

  @Test
  public void shouldUpdateFrom() {
    Widget widget = new WidgetDataBuilder().build();
    WidgetDto dto = WidgetDto.newInstance(widget);
    dto.setName("ala");

    widget.updateFrom(dto);

    assertThat(widget.getName()).isEqualTo("ala");
  }

  @Test
  public void shouldExportData() {
    Widget widget = new WidgetDataBuilder().build();
    WidgetDto dto = new WidgetDto();

    widget.export(dto);

    Assertions.assertThat(dto.getId()).isEqualTo(widget.getId());
    assertThat(dto.getName()).isEqualTo(widget.getName());
  }
}
