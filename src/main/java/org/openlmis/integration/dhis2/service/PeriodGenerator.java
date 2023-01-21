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

package org.openlmis.integration.dhis2.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import org.openlmis.integration.dhis2.domain.enumerators.DhisPeriods;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class PeriodGenerator {

  private final Clock clock;

  public PeriodGenerator() {
    this.clock = Clock.systemDefaultZone();
  }

  public PeriodGenerator(Clock clock) {
    this.clock = clock;
  }

  public Pair<ZonedDateTime, ZonedDateTime> generateRange(
          DhisPeriods periodEnum, Long offsetMinutes) {
    Pair<ZonedDateTime, ZonedDateTime> range = periodEnum.generate(ZonedDateTime.now(this.clock));
    return Pair.of(range.getFirst().plusMinutes(offsetMinutes), range.getSecond());
  }

}
