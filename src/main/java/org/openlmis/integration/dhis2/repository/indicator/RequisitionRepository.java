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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RequisitionRepository {

  @Query(value = "SELECT SUM(line_items.beginningbalance)"
          + "FROM requisition.requisition_line_items AS line_items"
          + "JOIN referencedata.orderables AS products ON line_items.orderableid = products.id"
          + "JOIN requisition.requisitions AS req ON line_items.requisitionid = req.id"
          + "WHERE products.versionnumber = ("
          + "SELECT MAX(versionnumber) FROM referencedata.orderables o2 WHERE o2.id = products.id"
          + ") AND req.createddate >= :startDate"
          + "AND req.createddate < :endDate", nativeQuery = true)
  BigDecimal findOpeningBalance(@Param("startDate") ZonedDateTime startDate,
                          @Param("endDate") ZonedDateTime endDate);

  @Query(value = "SELECT SUM(line_items.totalreceivedquantity)"
          + "FROM requisition.requisition_line_items AS line_items"
          + "JOIN referencedata.orderables AS products ON line_items.orderableid = products.id"
          + "JOIN requisition.requisitions AS req ON line_items.requisitionid = req.id"
          + "WHERE products.versionnumber = ("
          + "SELECT MAX(versionnumber) FROM referencedata.orderables o2 WHERE o2.id = products.id"
          + ") AND req.createddate >= :startDate"
          + "AND req.createddate < :endDate", nativeQuery = true)
  BigDecimal findReceivedBalance(@Param("startDate") ZonedDateTime startDate,
                                 @Param("endDate") ZonedDateTime endDate);

  @Query(value = "SELECT SUM(line_items.stockonhand)"
          + "FROM requisition.requisition_line_items AS line_items"
          + "JOIN referencedata.orderables AS products ON line_items.orderableid = products.id"
          + "JOIN requisition.requisitions AS req ON line_items.requisitionid = req.id"
          + "WHERE products.versionnumber = ("
          + "SELECT MAX(versionnumber) FROM referencedata.orderables o2 WHERE o2.id = products.id"
          + ") AND req.createddate >= :startDate"
          + "AND req.createddate < :endDate", nativeQuery = true)
  BigDecimal findClosingBalance(@Param("startDate") ZonedDateTime startDate,
                          @Param("endDate") ZonedDateTime endDate);

}
