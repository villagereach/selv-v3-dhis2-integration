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
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;

@Entity
@TypeName("DataElement")
@Table(name = "data_element", schema = "dhis2")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class DataElement extends BaseEntity {

  @Column(unique = true)
  @NonNull
  @ToString.Include
  private String name;

  @Column
  @NonNull
  @ToString.Include
  private String source;

  @Column
  @NonNull
  @ToString.Include
  private String indicator;

  @Column
  @NonNull
  @ToString.Include
  private String orderable;

  @Column
  @NonNull
  @ToString.Include
  private String element;

  @Column
  @NonNull
  @ToString.Include
  private String categoryCombo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "datasetId", nullable = false)
  private Dataset dataset;

  @Column
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "dataElement")
  private List<Schedule> scheduleList = new ArrayList<>();

  /**
   * Creates new instance based on data from the importer.
   */
  public static DataElement newInstance(Importer importer) {
    DataElement dataElement = new DataElement();
    dataElement.setId(importer.getId());
    dataElement.updateFrom(importer);

    return dataElement;
  }

  /**
   * Updates entity from the importer.
   */
  public void updateFrom(Importer importer) {
    name = importer.getName();
    source = importer.getSource();
    indicator = importer.getIndicator();
    orderable = importer.getOrderable();
    element = importer.getElement();
    categoryCombo = importer.getCategoryCombo();
    dataset = importer.getDataset();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setName(name);
    exporter.setSource(source);
    exporter.setIndicator(indicator);
    exporter.setOrderable(orderable);
    exporter.setElement(element);
    exporter.setCategoryCombo(categoryCombo);
    exporter.setDataset(dataset);
  }

  public interface Exporter extends BaseExporter {
    void setName(String name);

    void setSource(String source);

    void setIndicator(String indicator);

    void setOrderable(String orderable);

    void setElement(String element);

    void setCategoryCombo(String categoryCombo);

    void setDataset(Dataset dataset);
  }

  public interface Importer extends BaseImporter {
    String getName();

    String getSource();

    String getIndicator();

    String getOrderable();

    String getElement();

    String getCategoryCombo();

    Dataset getDataset();
  }

}
