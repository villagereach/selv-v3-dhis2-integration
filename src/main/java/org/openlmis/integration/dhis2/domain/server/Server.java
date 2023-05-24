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
import org.openlmis.integration.dhis2.domain.facility.SharedFacility;
import org.openlmis.integration.dhis2.domain.periodmapping.PeriodMapping;
import org.openlmis.integration.dhis2.domain.schedule.Schedule;

@Entity
@TypeName("Server")
@Table(name = "server", schema = "dhis2")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class Server extends BaseEntity {

  @Column
  @NonNull
  @ToString.Include
  private String name;

  @Column
  @NonNull
  @ToString.Include
  private String url;

  @Column
  @NonNull
  @ToString.Include
  private String username;

  @Column
  @NonNull
  private String password;

  @Column
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "server")
  private List<Dataset> datasetList = new ArrayList<>();

  @Column
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "server")
  private List<Schedule> scheduleList = new ArrayList<>();

  @Column
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "server")
  private List<SharedFacility> sharedFacilityList = new ArrayList<>();

  @Column
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "server")
  private List<PeriodMapping> periodMappingList = new ArrayList<>();

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
