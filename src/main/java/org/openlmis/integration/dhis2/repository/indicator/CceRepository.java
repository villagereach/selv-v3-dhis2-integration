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

package org.openlmis.integration.dhis2.repository.indicator;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public class CceRepository {

  static final String STATUS = "status";
  static final String UTILIZATION = "utilization";

  @PersistenceContext
  EntityManager entityManager;

  /**
   * Retrieves CCE count for a given status.
   */
  public String findCceCountByStatus(@Param(STATUS) String status) {
    Query query = entityManager.createNativeQuery(
            "SELECT COUNT(inventory.functionalstatus) FROM "
                    + "cce.cce_inventory_items AS inventory "
                    + "WHERE functionalstatus = :status");

    return query.setParameter(status, status)
            .getSingleResult().toString();
  }

  /**
   * Retrieves CCE count for a given utilization.
   */
  public String findCceCountByUtilization(@Param(UTILIZATION) String utilization) {
    Query query = entityManager.createNativeQuery(
            "SELECT COUNT(inventory.utilization) FROM "
                    + "cce.cce_inventory_items AS inventory "
                    + "WHERE utilization = :utilization");

    return query.setParameter(utilization, utilization)
            .getSingleResult().toString();
  }

}
