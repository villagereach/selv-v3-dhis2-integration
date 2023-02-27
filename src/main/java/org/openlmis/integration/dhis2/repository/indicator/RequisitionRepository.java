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

import java.time.ZonedDateTime;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public class RequisitionRepository {

  static final String START_DATE = "startDate";
  static final String END_DATE = "endDate";
  static final String ORDERABLE = "orderable";
  static final String FACILITY = "facility";

  @PersistenceContext
  EntityManager entityManager;

  /**
   * Retrieves opening balance from requisition for a given period.
   */
  public Long findOpeningBalance(@Param(START_DATE) ZonedDateTime startDate,
                                   @Param(ORDERABLE) String orderable,
                                   @Param(FACILITY) String facility) {
    Query query = entityManager.createNativeQuery(
            "SELECT line_items.beginningbalance AS bb "
                    + "FROM requisition.requisition_line_items AS line_items "
                    + "JOIN referencedata.orderables AS products "
                    + "ON line_items.orderableid = products.id "
                    + "JOIN requisition.requisitions AS req ON line_items.requisitionid = req.id "
                    + "JOIN referencedata.facilities AS facilities "
                    + "ON facilities.id = req.facilityid "
                    + "WHERE products.versionnumber = "
                    + "(SELECT MAX(versionnumber) FROM referencedata.orderables o2 "
                    + "WHERE o2.id = products.id) "
                    + "AND line_items.beginningbalance NOTNULL "
                    + "AND req.createddate <= :startDate "
                    + "AND products.fullproductname = :orderable "
                    + "AND facilities.code = :facility "
                    + "ORDER BY req.createddate DESC LIMIT 1");

    return Long.parseLong(query.setParameter(START_DATE, startDate)
            .setParameter(ORDERABLE, orderable)
            .setParameter(FACILITY, facility)
            .getSingleResult().toString());
  }

  /**
   * Retrieves closing balance from requisition for a given period.
   */
  public Long findClosingBalance(@Param(END_DATE) ZonedDateTime endDate,
                                   @Param(ORDERABLE) String orderable,
                                   @Param(FACILITY) String facility) {
    Query query = entityManager.createNativeQuery(
            "SELECT line_items.stockonhand AS soh "
                    + "FROM requisition.requisition_line_items AS line_items "
                    + "JOIN referencedata.orderables AS products "
                    + "ON line_items.orderableid = products.id "
                    + "JOIN requisition.requisitions AS req ON line_items.requisitionid = req.id "
                    + "JOIN referencedata.facilities AS facilities "
                    + "ON facilities.id = req.facilityid "
                    + "WHERE products.versionnumber = "
                    + "(SELECT MAX(versionnumber) FROM referencedata.orderables o2 "
                    + "WHERE o2.id = products.id) "
                    + "AND line_items.stockonhand NOTNULL "
                    + "AND req.createddate <= :endDate "
                    + "AND products.fullproductname = :orderable "
                    + "AND facilities.code = :facility "
                    + "ORDER BY req.createddate DESC LIMIT 1");

    return Long.parseLong(query.setParameter(END_DATE, endDate)
            .setParameter(ORDERABLE, orderable)
            .setParameter(FACILITY, facility)
            .getSingleResult().toString());
  }

  /**
   * Retrieves received amount of products from requisition for a given period.
   */
  public Double findReceived(@Param(END_DATE) ZonedDateTime endDate,
                                   @Param(ORDERABLE) String orderable,
                                   @Param(FACILITY) String facility) {
    Query query = entityManager.createNativeQuery(
            "SELECT line_items.totalreceivedquantity AS received "
                    + "FROM requisition.requisition_line_items AS line_items "
                    + "JOIN referencedata.orderables AS products "
                    + "ON line_items.orderableid = products.id "
                    + "JOIN requisition.requisitions AS req ON line_items.requisitionid = req.id "
                    + "JOIN referencedata.facilities AS facilities "
                    + "ON facilities.id = req.facilityid "
                    + "WHERE products.versionnumber = "
                    + "(SELECT MAX(versionnumber) FROM referencedata.orderables o2 "
                    + "WHERE o2.id = products.id) "
                    + "AND line_items.totalreceivedquantity NOTNULL "
                    + "AND req.createddate <= :endDate "
                    + "AND products.fullproductname = :orderable "
                    + "AND facilities.code = :facility "
                    + "ORDER BY req.createddate desc LIMIT 1");

    return Double.parseDouble(query.setParameter(END_DATE, endDate)
            .setParameter(ORDERABLE, orderable)
            .setParameter(FACILITY, facility)
            .getSingleResult().toString());
  }

}
