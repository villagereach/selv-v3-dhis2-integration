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

package org.openlmis.integration.dhis2.domain.server;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
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
@TypeName("Server")
@Table(name = "server", schema = "dhis2")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Server extends BaseEntity {

  @Column(nullable = false)
  @Getter
  @Setter
  private String name;

  @Column(nullable = false)
  @Getter
  @Setter
  private String url;

  @Column(nullable = false)
  @Getter
  @Setter
  private String username;

  @Column(nullable = false)
  @Getter
  @Setter
  private String password;

  @Column
  @Getter
  @Setter
  @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "server")
  private List<Dataset> datasetList = new ArrayList<>();

  /**
   * Creates new instance based on data from the importer.
   */
  public static Server newInstance(Importer importer) {
    Server server = new Server();
    server.setId(importer.getId());
    server.updateFrom(importer);

    return server;
  }

  /**
   * Updates entity from the importer.
   */
  public void updateFrom(Importer importer) {
    name = importer.getName();
    url = importer.getUrl();
    username = importer.getUsername();
    password = importer.getPassword();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setName(name);
    exporter.setUrl(url);
    exporter.setUsername(username);
    exporter.setPassword(password);
  }

  public interface Exporter extends BaseExporter {
    void setName(String name);

    void setUrl(String url);

    void setUsername(String username);

    void setPassword(String password);
  }

  public interface Importer extends BaseImporter {
    String getName();

    String getUrl();

    String getUsername();

    String getPassword();
  }

}
