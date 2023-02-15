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
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class StockmanagementRepository {

  static final String START_DATE = "startDate";
  static final String END_DATE = "endDate";
  static final String ORDERABLE = "orderable";
  static final String FACILITY = "facility";

  @PersistenceContext
  EntityManager entityManager;

  /**
   * Retrieves opening balance from stockmanagement for a given period.
   */
  public String findOpeningBalance(@Param(START_DATE) ZonedDateTime startDate,
                                   @Param(ORDERABLE) String orderable,
                                   @Param(FACILITY) String facility) {
    Query query = entityManager.createNativeQuery(
            "SELECT cal.stockonhand "
                    + "FROM stockmanagement.stock_card_line_items AS line_items  "
                    + "JOIN stockmanagement.stock_cards AS cards "
                    + "ON line_items.stockcardid = cards.id "
                    + "JOIN stockmanagement.stock_card_line_item_reasons AS reasons "
                    + "ON reasons.id = line_items.reasonid "
                    + "JOIN stockmanagement.calculated_stocks_on_hand AS cal "
                    + "ON cal.stockcardid = cards.id "
                    + "JOIN referencedata.orderables AS products "
                    + "ON cards.orderableid = products.id  "
                    + "JOIN referencedata.facilities AS facilities "
                    + "ON facilities.id = cards.facilityid  "
                    + "WHERE products.versionnumber = ( "
                    + "SELECT MAX(versionnumber) FROM referencedata.orderables o2 "
                    + "WHERE o2.id = products.id "
                    + ") "
                    + "AND line_items.occurreddate <= :startDate "
                    + "AND products.fullproductname = :orderable  "
                    + "AND facilities.code = :facility  "
                    + "ORDER BY line_items.occurreddate DESC LIMIT 1");

    return query.setParameter(START_DATE, startDate)
            .setParameter(ORDERABLE, orderable)
            .setParameter(FACILITY, facility)
            .getSingleResult().toString();
  }

  /**
   * Retrieves closing balance from stockmanagement for a given period.
   */
  public String findClosingBalance(@Param(END_DATE) ZonedDateTime endDate,
                                   @Param(ORDERABLE) String orderable,
                                   @Param(FACILITY) String facility) {
    Query query = entityManager.createNativeQuery(
            "SELECT cal.stockonhand "
                    + "FROM stockmanagement.stock_card_line_items AS line_items  "
                    + "JOIN stockmanagement.stock_cards AS cards "
                    + "ON line_items.stockcardid = cards.id "
                    + "JOIN stockmanagement.stock_card_line_item_reasons AS reasons "
                    + "ON reasons.id = line_items.reasonid "
                    + "JOIN stockmanagement.calculated_stocks_on_hand AS cal "
                    + "ON cal.stockcardid = cards.id "
                    + "JOIN referencedata.orderables AS products "
                    + "ON cards.orderableid = products.id  "
                    + "JOIN referencedata.facilities AS facilities "
                    + "ON facilities.id = cards.facilityid  "
                    + "WHERE products.versionnumber = ( "
                    + "SELECT MAX(versionnumber) FROM referencedata.orderables o2 "
                    + "WHERE o2.id = products.id "
                    + ") "
                    + "AND line_items.occurreddate <= :endDate "
                    + "AND products.fullproductname = :orderable  "
                    + "AND facilities.code = :facility  "
                    + "ORDER BY line_items.occurreddate DESC LIMIT 1");

    return query.setParameter(END_DATE, endDate)
            .setParameter(ORDERABLE, orderable)
            .setParameter(FACILITY, facility)
            .getSingleResult().toString();
  }

  /**
   * Retrieves received amount of products from stockmanagement for a given period.
   */
  public String findReceived(@Param(START_DATE) ZonedDateTime startDate,
                             @Param(END_DATE) ZonedDateTime endDate,
                             @Param(ORDERABLE) String orderable,
                             @Param(FACILITY) String facility) {
    Query query = entityManager.createNativeQuery(
            "SELECT SUM(line_items.quantity) AS quantity "
                    + "FROM stockmanagement.stock_card_line_items AS line_items  "
                    + "JOIN stockmanagement.stock_cards AS cards "
                    + "ON line_items.stockcardid = cards.id "
                    + "JOIN stockmanagement.stock_card_line_item_reasons AS reasons "
                    + "ON reasons.id = line_items.reasonid "
                    + "JOIN referencedata.orderables AS products "
                    + "ON cards.orderableid = products.id  "
                    + "JOIN referencedata.facilities AS facilities "
                    + "ON facilities.id = cards.facilityid  "
                    + "WHERE products.versionnumber = ( "
                    + "SELECT MAX(versionnumber) FROM referencedata.orderables o2 "
                    + "WHERE o2.id = products.id "
                    + ") "
                    + "AND reasons.reasoncategory = 'TRANSFER' "
                    + "AND reasons.reasontype = 'CREDIT' "
                    + "AND line_items.occurreddate >= :startDate "
                    + "AND line_items.occurreddate < :endDate "
                    + "AND products.fullproductname = :orderable  "
                    + "AND facilities.code = :facility ");

    return query.setParameter(START_DATE, startDate)
            .setParameter(END_DATE, endDate)
            .setParameter(ORDERABLE, orderable)
            .setParameter(FACILITY, facility)
            .getSingleResult().toString();
  }

  /**
   * Retrieves sum of all positive adjustments from stockmanagement for a given period.
   */
  public String findPositiveAdjustments(@Param(START_DATE) ZonedDateTime startDate,
                                        @Param(END_DATE) ZonedDateTime endDate,
                                        @Param(ORDERABLE) String orderable,
                                        @Param(FACILITY) String facility) {
    Query query = entityManager.createNativeQuery(
            "SELECT SUM(line_items.quantity) AS quantity "
                    + "FROM stockmanagement.stock_card_line_items AS line_items  "
                    + "JOIN stockmanagement.stock_cards AS cards "
                    + "ON line_items.stockcardid = cards.id "
                    + "JOIN stockmanagement.stock_card_line_item_reasons AS reasons "
                    + "ON reasons.id = line_items.reasonid "
                    + "JOIN referencedata.orderables AS products "
                    + "ON cards.orderableid = products.id  "
                    + "JOIN referencedata.facilities AS facilities "
                    + "ON facilities.id = cards.facilityid  "
                    + "WHERE products.versionnumber = ( "
                    + "SELECT MAX(versionnumber) FROM referencedata.orderables o2 "
                    + "WHERE o2.id = products.id "
                    + ") "
                    + "AND reasons.reasoncategory = 'ADJUSTMENT' "
                    + "AND reasons.reasontype = 'CREDIT' "
                    + "AND line_items.occurreddate >= :startDate "
                    + "AND line_items.occurreddate < :endDate "
                    + "AND products.fullproductname = :orderable  "
                    + "AND facilities.code = :facility ");

    return query.setParameter(START_DATE, startDate)
            .setParameter(END_DATE, endDate)
            .setParameter(ORDERABLE, orderable)
            .setParameter(FACILITY, facility)
            .getSingleResult().toString();
  }

  /**
   * Retrieves sum of all negative adjustments from stockmanagement for a given period.
   */
  public String findNegativeAdjustments(@Param(START_DATE) ZonedDateTime startDate,
                                        @Param(END_DATE) ZonedDateTime endDate,
                                        @Param(ORDERABLE) String orderable,
                                        @Param(FACILITY) String facility) {
    Query query = entityManager.createNativeQuery(
            "SELECT SUM(line_items.quantity) AS quantity "
                    + "FROM stockmanagement.stock_card_line_items AS line_items  "
                    + "JOIN stockmanagement.stock_cards AS cards "
                    + "ON line_items.stockcardid = cards.id "
                    + "JOIN stockmanagement.stock_card_line_item_reasons AS reasons "
                    + "ON reasons.id = line_items.reasonid "
                    + "JOIN referencedata.orderables AS products "
                    + "ON cards.orderableid = products.id  "
                    + "JOIN referencedata.facilities AS facilities "
                    + "ON facilities.id = cards.facilityid  "
                    + "WHERE products.versionnumber = ( "
                    + "SELECT MAX(versionnumber) FROM referencedata.orderables o2 "
                    + "WHERE o2.id = products.id "
                    + ") "
                    + "AND reasons.reasoncategory = 'ADJUSTMENT' "
                    + "AND reasons.reasontype = 'DEBIT' "
                    + "AND line_items.occurreddate >= :startDate "
                    + "AND line_items.occurreddate < :endDate "
                    + "AND products.fullproductname = :orderable  "
                    + "AND facilities.code = :facility ");

    return query.setParameter(START_DATE, startDate)
            .setParameter(END_DATE, endDate)
            .setParameter(ORDERABLE, orderable)
            .setParameter(FACILITY, facility)
            .getSingleResult().toString();
  }

}
