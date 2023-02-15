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

package org.openlmis.integration.dhis2.domain.enumerator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.function.Function;
import org.springframework.data.util.Pair;

public enum DhisPeriod {

  DAILY(DhisPeriod::generateForDaily),
  WEEKLY_MONDAY(now -> DhisPeriod.generateForWeekly(now, DayOfWeek.MONDAY)),
  WEEKLY_TUESDAY(now -> DhisPeriod.generateForWeekly(now, DayOfWeek.TUESDAY)),
  WEEKLY_WEDNESDAY(now -> DhisPeriod.generateForWeekly(now, DayOfWeek.WEDNESDAY)),
  WEEKLY_THURSDAY(now -> DhisPeriod.generateForWeekly(now, DayOfWeek.THURSDAY)),
  WEEKLY_FRIDAY(now -> DhisPeriod.generateForWeekly(now, DayOfWeek.FRIDAY)),
  WEEKLY_SATURDAY(now -> DhisPeriod.generateForWeekly(now, DayOfWeek.SATURDAY)),
  WEEKLY_SUNDAY(now -> DhisPeriod.generateForWeekly(now, DayOfWeek.SUNDAY)),
  MONTHLY(DhisPeriod::generateForMonthly);

  private final Function<ZonedDateTime, Pair<ZonedDateTime, ZonedDateTime>> generator;

  DhisPeriod(Function<ZonedDateTime, Pair<ZonedDateTime, ZonedDateTime>> generator) {
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

  /**
   * Generates date range for a given period enumerator and certain date
   *
   *<p>Both dates will be recorded at 0:00 midnight
   *
   *<p>e.g. MONTHLY.generate(ZonedDateTime.now()) will generate full month for a given date
   *
   * @param now Current date-time
   * @return Pair containing starting date and end date
   */
  public Pair<ZonedDateTime, ZonedDateTime> generate(ZonedDateTime now) {
    return this.generator.apply(now);
  }

  /**
   * Returns ISO code for a given period enumerator.
   *
   * @param periodKey Period enumerator
   * @return ISO date format
   */
  public static String getIsoPattern(DhisPeriod periodKey) {
    EnumMap<DhisPeriod, String> isoPatternMap
            = new EnumMap<>(DhisPeriod.class);
    isoPatternMap.put(DAILY, "yyyyMMdd");
    isoPatternMap.put(WEEKLY_MONDAY, "yyyyMonWn");
    isoPatternMap.put(WEEKLY_TUESDAY, "yyyyTueWn");
    isoPatternMap.put(WEEKLY_WEDNESDAY, "yyyyWedWn");
    isoPatternMap.put(WEEKLY_THURSDAY, "yyyyThuWn");
    isoPatternMap.put(WEEKLY_FRIDAY, "yyyyFriWn");
    isoPatternMap.put(WEEKLY_SATURDAY, "yyyySatWn");
    isoPatternMap.put(WEEKLY_SUNDAY, "yyyySunWn");
    isoPatternMap.put(MONTHLY, "yyyyMM");
    return isoPatternMap.get(periodKey);
  }

}
