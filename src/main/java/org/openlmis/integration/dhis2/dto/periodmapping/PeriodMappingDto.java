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

package org.openlmis.integration.dhis2.dto.periodmapping;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.integration.dhis2.domain.periodmapping.PeriodMapping;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.BaseDto;
import org.openlmis.integration.dhis2.dto.server.ServerDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class PeriodMappingDto extends BaseDto implements PeriodMapping.Importer,
        PeriodMapping.Exporter {

  private String name;
  private String source;
  private String dhisPeriod;
  private UUID processingPeriodId;

  @JsonFormat(shape = STRING)
  private LocalDate startDate;

  @JsonFormat(shape = STRING)
  private LocalDate endDate;

  private ServerDto serverDto;

  /**
   * Creates new instance based on domain object.
   */
  public static PeriodMappingDto newInstance(PeriodMapping periodMapping) {
    PeriodMappingDto dto = new PeriodMappingDto();
    periodMapping.export(dto);
    dto.setServer(periodMapping.getServer());
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
