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

@Entity
@TypeName("DataElement")
@Table(name = "data_element", schema = "dhis2")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataElement extends BaseEntity {

  @Column(nullable = false, unique = true)
  @Getter
  @Setter
  private String name;

  @Column(nullable = false)
  @Getter
  @Setter
  private String source;

  @Column(nullable = false)
  @Getter
  @Setter
  private String indicator;

  @Column(nullable = false)
  @Getter
  @Setter
  private String orderable;

  @Column(nullable = false)
  @Getter
  @Setter
  private String element;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "datasetId", nullable = false)
  @Getter
  @Setter
  private Dataset dataset;

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
  }

  public interface Exporter extends BaseExporter {

    void setName(String name);

    void setSource(String source);

    void setIndicator(String indicator);

    void setOrderable(String orderable);

    void setElement(String element);

    void setDataset(Dataset dataset);

  }

  public interface Importer extends BaseImporter {

    String getName();

    String getSource();

    String getIndicator();

    String getOrderable();

    String getElement();

    Dataset getDataset();

  }

}
