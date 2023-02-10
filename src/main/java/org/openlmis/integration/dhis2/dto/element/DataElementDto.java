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

package org.openlmis.integration.dhis2.dto.element;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.dto.BaseDto;
import org.openlmis.integration.dhis2.dto.dataset.DatasetDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DataElementDto extends BaseDto implements
        DataElement.Importer, DataElement.Exporter {

  private String name;
  private String source;
  private String indicator;
  private String orderable;
  private String element;
  private DatasetDto datasetDto;

  /**
   * Creates new instance based on domain object.
   */
  public static DataElementDto newInstance(DataElement dataElement) {
    DataElementDto dto = new DataElementDto();
    dataElement.export(dto);
    dto.setDataset(dataElement.getDataset());
    return dto;
  }

  @JsonIgnore
  @Override
  public void setDataset(Dataset dataset) {
    this.datasetDto = DatasetDto.newInstance(dataset);
  }

  @Override
  public Dataset getDataset() {
    return Dataset.newInstance(datasetDto);
  }

}
