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

package org.openlmis.integration.dhis2.domain.facility;

import java.util.UUID;
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
import org.openlmis.integration.dhis2.domain.server.Server;

@Entity
@TypeName("SharedFacility")
@Table(name = "shared_facility", schema = "dhis2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class SharedFacility extends BaseEntity {

  @Column(nullable = false)
  @ToString.Include
  private String code;

  @Column(nullable = false)
  @ToString.Include
  private UUID facilityId;

  @Column(nullable = false)
  @ToString.Include
  private String orgUnitId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "serverId", nullable = false)
  private Server server;

  /**
   * Creates new instance based on data from the importer.
   */
  public static SharedFacility newInstance(Importer importer) {
    SharedFacility sharedFacility = new SharedFacility();
    sharedFacility.setId(importer.getId());
    sharedFacility.updateFrom(importer);

    return sharedFacility;
  }

  /**
   * Updates entity from the importer.
   */
  public void updateFrom(Importer importer) {
    code = importer.getCode();
    facilityId = importer.getFacilityId();
    orgUnitId = importer.getOrgUnitId();
    server = importer.getServer();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setCode(getCode());
    exporter.setFacilityId(getFacilityId());
    exporter.setOrgUnitId(getOrgUnitId());
    exporter.setServer(server);
  }

  public interface Exporter extends BaseExporter {
    void setCode(String code);

    void setFacilityId(UUID facilityId);

    void setOrgUnitId(String orgUnitId);

    void setServer(Server server);
  }

  public interface Importer extends BaseImporter {
    String getCode();

    UUID getFacilityId();

    String getOrgUnitId();

    Server getServer();
  }

}
