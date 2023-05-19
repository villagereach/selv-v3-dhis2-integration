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

import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.integration.dhis2.domain.BaseEntity;
import org.openlmis.integration.dhis2.domain.server.Server;

@Entity
@TypeName("PeriodMapping")
@Table(name = "period_mapping", schema = "dhis2")
@Getter
@Setter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = false)
public class PeriodMapping extends BaseEntity {

  @Column(unique = true)
  @NonNull
  private String name;

  @Column(unique = true)
  @NonNull
  private String source;

  @Column
  @NonNull
  private String dhisPeriod;

  @Column
  @NonNull
  private UUID processingPeriodId;

  @Column
  @NonNull
  private LocalDate startDate;

  @Column
  @NonNull
  private LocalDate endDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "serverId", nullable = false)
  private Server server;

  /**
   * Creates new instance based on data from the importer.
   */
  public static PeriodMapping newInstance(Importer importer) {
    PeriodMapping periodMapping = new PeriodMapping();
    periodMapping.setId(importer.getId());
    periodMapping.updateFrom(importer);

    return periodMapping;
  }

  /**
   * Updates entity from the importer.
   */
  public void updateFrom(Importer importer) {
    name = importer.getName();
    source = importer.getSource();
    dhisPeriod = importer.getDhisPeriod();
    processingPeriodId = importer.getProcessingPeriodId();
    startDate = importer.getStartDate();
    endDate = importer.getEndDate();
    server = importer.getServer();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setName(name);
    exporter.setSource(source);
    exporter.setDhisPeriod(dhisPeriod);
    exporter.setProcessingPeriodId(processingPeriodId);
    exporter.setStartDate(startDate);
    exporter.setEndDate(endDate);
    exporter.setServer(server);
  }

  public interface Exporter extends BaseExporter {

    void setName(String name);

    void setSource(String source);

    void setDhisPeriod(String dhisPeriod);

    void setProcessingPeriodId(UUID processingPeriodId);

    void setStartDate(LocalDate startDate);

    void setEndDate(LocalDate endDate);

    void setServer(Server server);

  }

  public interface Importer extends BaseImporter {

    String getName();

    String getSource();

    String getDhisPeriod();

    UUID getProcessingPeriodId();

    LocalDate getStartDate();

    LocalDate getEndDate();

    Server getServer();

  }

}
