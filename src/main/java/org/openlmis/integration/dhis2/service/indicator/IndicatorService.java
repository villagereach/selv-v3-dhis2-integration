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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.openlmis.integration.dhis2.domain.enumerator.IndicatorEnum;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class IndicatorService {

  /**
   * Counts quantity of items for a given indicator enumerator.
   *
   * @param indicatorEnum Enumerator used to calculate indicators
   * @param period Pair containing starting date and end date
   * @return BigDecimal with generated indicator value for a given period
   */
  public BigDecimal generate(IndicatorEnum indicatorEnum, Pair<ZonedDateTime,
          ZonedDateTime> period) {
    BigDecimal calculatedIndicator = BigDecimal.ZERO;
    switch (indicatorEnum) {
      case OPENING_BALANCE:
        calculatedIndicator = new OpeningBalance().calculateValue(period);
        break;
      case RECEIVED:
        calculatedIndicator = new ReceivedBalance().calculateValue(period);
        break;
      case CLOSING_BALANCE:
        calculatedIndicator = new ClosingBalance().calculateValue(period);
        break;
      case CCE_ALLOCATED:
        calculatedIndicator = new CceAllocated().calculateValue(period);
        break;
      case CCE_OPERATIONAL:
        calculatedIndicator = new CceOperational().calculateValue(period);
        break;
      case NEGATIVE_ADJUSTMENTS:
        break;
      case POSITIVE_ADJUSTMENTS:
        break;
      case ADJUSTMENTS_BY_REASON:
        break;
      default:
        throw new NotFoundException(MessageKeys.ERROR_INDICATOR_NOT_FOUND);
    }

    return calculatedIndicator;
  }

}
