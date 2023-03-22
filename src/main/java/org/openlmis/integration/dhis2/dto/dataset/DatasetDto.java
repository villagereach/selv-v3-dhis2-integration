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

package org.openlmis.integration.dhis2.dto.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.BaseDto;
import org.openlmis.integration.dhis2.dto.server.ServerDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DatasetDto extends BaseDto implements Dataset.Importer, Dataset.Exporter {

  private String name;
  private String dhisDatasetId;
  private String cronExpression;
  private Integer timeOffset;
  private ServerDto serverDto;

  /**
   * Creates new instance based on domain object.
   */
  public static DatasetDto newInstance(Dataset dataset) {
    DatasetDto dto = new DatasetDto();
    dataset.export(dto);
    dto.setServer(dataset.getServer());
    return dto;
  }

  @JsonIgnore
  @Override
  public void setServer(Server server) {
    this.serverDto = ServerDto.newInstance(server);
  }

  @Override
  public Server getServer() {
    return Server.newInstance(serverDto);
  }

}
