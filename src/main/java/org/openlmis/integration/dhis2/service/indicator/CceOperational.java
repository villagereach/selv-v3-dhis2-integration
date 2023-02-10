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
import org.openlmis.integration.dhis2.repository.indicator.CceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

public class CceOperational implements IndicatorSupplier {

  public static final String STATUS = "FUNCTIONING";
  public static final String NAME = IndicatorEnum.CCE_OPERATIONAL.toString();

  @Autowired
  private CceRepository cceRepository;

  public String getIndicatorName() {
    return NAME;
  }

  public BigDecimal calculateValue(Pair<ZonedDateTime, ZonedDateTime> period) {
    return cceRepository.findCceCountByStatus(STATUS, period.getFirst(), period.getSecond());
  }

}