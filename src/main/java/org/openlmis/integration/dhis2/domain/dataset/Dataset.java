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

package org.openlmis.integration.dhis2.domain.dataset;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;
import org.openlmis.integration.dhis2.domain.server.Server;

@Entity
@TypeName("Dataset")
@Table(name = "dataset", schema = "dhis2")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Dataset extends BaseEntity {

  @Column
  @NonNull
  private String name;

  @Column
  @NonNull
  private String dhisDatasetId;

  @Column
  @NonNull
  private String cronExpression;

  @Column
  @NonNull
  private Integer timeOffset;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "serverId", nullable = false)
  private Server server;

  @Column
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "dataset")
  private List<DataElement> dataElementList = new ArrayList<>();

  @Column
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "dataset")
  private List<Schedule> scheduleList = new ArrayList<>();

  /**
   * Creates new instance based on data from the importer.
   */
  public static Dataset newInstance(Importer importer) {
    Dataset dataset = new Dataset();
    dataset.setId(importer.getId());
    dataset.updateFrom(importer);

    return dataset;
  }

  /**
   * Updates entity from the importer.
   */
  public void updateFrom(Importer importer) {
    name = importer.getName();
    dhisDatasetId = importer.getDhisDatasetId();
    cronExpression = importer.getCronExpression();
    timeOffset = importer.getTimeOffset();
    server = importer.getServer();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setName(name);
    exporter.setDhisDatasetId(dhisDatasetId);
    exporter.setCronExpression(cronExpression);
    exporter.setTimeOffset(timeOffset);
    exporter.setServer(server);
  }

  public interface Exporter extends BaseExporter {

    void setName(String name);

    void setDhisDatasetId(String dhisDatasetId);

    void setCronExpression(String cronExpression);

    void setTimeOffset(Integer timeOffset);

    void setServer(Server server);

  }

  public interface Importer extends BaseImporter {

    String getName();

    String getDhisDatasetId();

    String getCronExpression();

    Integer getTimeOffset();

    Server getServer();

  }

}
