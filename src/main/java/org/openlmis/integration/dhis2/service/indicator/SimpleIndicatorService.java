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

import java.time.ZonedDateTime;
import org.openlmis.integration.dhis2.domain.enumerator.DhisPeriod;
import org.openlmis.integration.dhis2.repository.indicator.RequisitionRepository;
import org.openlmis.integration.dhis2.service.PeriodGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class SimpleIndicatorService {

  @Autowired
  RequisitionRepository requisitionRepository;

  /**
   * Counts opening balance for opening balance indicator within certain period.
   *
   * @param periodEnum Enumerator used to calculate a pair of starting and end date
   * @param offset Offset date in seconds
   * @param orderable Product code
   * @param facility Facility code
   * @return String with generated indicator value for a given period
   */
  public String generateOpeningBalance(DhisPeriod periodEnum, Long offset,
                                       String orderable, String facility) {
    Pair<ZonedDateTime, ZonedDateTime> period = new PeriodGenerator()
            .generateRange(periodEnum, offset);
    return requisitionRepository.findOpeningBalance(period.getFirst(), orderable, facility);
  }

}
