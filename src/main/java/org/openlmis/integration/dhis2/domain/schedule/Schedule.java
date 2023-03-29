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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.integration.dhis2.domain.BaseEntity;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.server.Server;

@Entity
@TypeName("Schedule")
@Table(name = "schedule", schema = "dhis2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Schedule extends BaseEntity {

  @Column(nullable = false)
  @ToString.Include
  private String periodEnumerator;

  @Column(nullable = false)
  @ToString.Include
  private int timeOffset;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "serverId", nullable = false)
  private Server server;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "datasetId", nullable = false)
  private Dataset dataset;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "elementId", nullable = false)
  private DataElement dataElement;

  /**
   * Creates new instance based on data from the importer.
   */
  public static Schedule newInstance(Importer importer) {
    Schedule schedule = new Schedule();
    schedule.setId(importer.getId());
    schedule.updateFrom(importer);

    return schedule;
  }

  /**
   * Updates entity from the importer.
   */
  public void updateFrom(Importer importer) {
    periodEnumerator = importer.getPeriodEnumerator();
    timeOffset = importer.getTimeOffset();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setPeriodEnumerator(getPeriodEnumerator());
    exporter.setTimeOffset(getTimeOffset());
  }

  public interface Exporter extends BaseExporter {
    void setPeriodEnumerator(String periodEnumerator);

    void setTimeOffset(int timeOffset);
  }

  public interface Importer extends BaseImporter {
    String getPeriodEnumerator();

    int getTimeOffset();
  }

}
