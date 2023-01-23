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

package org.openlmis.integration.dhis2.domain.enumerators;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Function;
import org.springframework.data.util.Pair;

public enum DhisPeriods {

  DAILY(DhisPeriods::generateForDaily),
  WEEKLY_MONDAY(now -> DhisPeriods.generateForWeekly((ZonedDateTime) now, DayOfWeek.MONDAY)),
  WEEKLY_TUESDAY(now -> DhisPeriods.generateForWeekly(now, DayOfWeek.TUESDAY)),
  WEEKLY_WEDNESDAY(now -> DhisPeriods.generateForWeekly(now, DayOfWeek.WEDNESDAY)),
  WEEKLY_THURSDAY(now -> DhisPeriods.generateForWeekly(now, DayOfWeek.THURSDAY)),
  WEEKLY_FRIDAY(now -> DhisPeriods.generateForWeekly(now, DayOfWeek.FRIDAY)),
  WEEKLY_SATURDAY(now -> DhisPeriods.generateForWeekly(now, DayOfWeek.SATURDAY)),
  WEEKLY_SUNDAY(now -> DhisPeriods.generateForWeekly(now, DayOfWeek.SUNDAY)),
  MONTHLY(DhisPeriods::generateForMonthly);

  private final Function<ZonedDateTime, Pair<ZonedDateTime, ZonedDateTime>> generator;

  DhisPeriods(Function<ZonedDateTime, Pair<ZonedDateTime, ZonedDateTime>> generator) {
    this.generator = generator;
  }

  private static Pair<ZonedDateTime, ZonedDateTime> generateForDaily(ZonedDateTime now) {
    ZoneId zoneId = now.getZone();
    LocalDateTime startDate = now.toLocalDate().atStartOfDay();
    LocalDateTime endDate = now.plusDays(1).toLocalDate().atStartOfDay();
    return Pair.of(ZonedDateTime.of(startDate, zoneId), ZonedDateTime.of(endDate, zoneId));
  }

  private static Pair<ZonedDateTime, ZonedDateTime> generateForWeekly(
          ZonedDateTime now, DayOfWeek dayOfWeek) {
    ZoneId zoneId = now.getZone();
    LocalDateTime startDate = now.with(TemporalAdjusters.previousOrSame(dayOfWeek))
            .toLocalDate().atStartOfDay();
    LocalDateTime endDate = now.with(TemporalAdjusters.nextOrSame(dayOfWeek))
            .toLocalDate().atStartOfDay();
    return Pair.of(ZonedDateTime.of(startDate, zoneId), ZonedDateTime.of(endDate, zoneId));
  }

  private static Pair<ZonedDateTime, ZonedDateTime> generateForMonthly(ZonedDateTime now) {
    ZoneId zoneId = now.getZone();
    LocalDateTime startDate = now.with(TemporalAdjusters.firstDayOfMonth())
            .toLocalDate().atStartOfDay();
    LocalDateTime endDate = now.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1)
            .toLocalDate().atStartOfDay();
    return Pair.of(ZonedDateTime.of(startDate, zoneId), ZonedDateTime.of(endDate, zoneId));
  }

  public Pair<ZonedDateTime, ZonedDateTime> generate(ZonedDateTime now) {
    return this.generator.apply(now);
  }

}
