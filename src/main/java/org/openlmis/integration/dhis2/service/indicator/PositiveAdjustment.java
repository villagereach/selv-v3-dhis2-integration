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

package org.openlmis.integration.dhis2.service.indicator;

import static org.openlmis.integration.dhis2.i18n.MessageKeys.ERROR_ENUMERATOR_NOT_EXIST;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.ZonedDateTime;
import org.openlmis.integration.dhis2.domain.enumerator.IndicatorEnum;
import org.openlmis.integration.dhis2.exception.ValidationMessageException;
import org.openlmis.integration.dhis2.repository.indicator.StockmanagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
public class PositiveAdjustment implements IndicatorSupplier {

  public static final String NAME = IndicatorEnum.POSITIVE_ADJUSTMENTS.toString();

  @Autowired
  private StockmanagementRepository stockmanagementRepository;

  public String getIndicatorName() {
    return NAME;
  }

  /**
   * Calculate positive adjustments.
   */
  public BigDecimal calculateValue(String source, Pair<ZonedDateTime, ZonedDateTime> period,
                                   String orderable, String facility) {
    Double calculatedIndicator;
    if (source.equals(STOCKMANAGEMENT)) {
      calculatedIndicator = stockmanagementRepository.findPositiveAdjustments(
              period.getFirst(), period.getSecond(), orderable, facility);
    } else {
      throw new ValidationMessageException(ERROR_ENUMERATOR_NOT_EXIST);
    }

    return new BigDecimal(calculatedIndicator.toString(), MathContext.DECIMAL64);
  }

}
