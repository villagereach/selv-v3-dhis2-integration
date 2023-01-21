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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.integration.dhis2.domain.enumerators.DhisPeriods;
import org.springframework.data.util.Pair;

@RunWith(MockitoJUnitRunner.class)
public class PeriodGeneratorTest {

  private static final Instant mondayInstant = Instant.parse("2023-01-16T00:00:00.00Z");
  private static final Instant tuesdayInstant = Instant.parse("2023-01-17T00:00:00.00Z");
  private static final Instant sundayInstant = Instant.parse("2023-01-22T00:00:00.00Z");
  private static final Instant januaryFirstInstant = Instant.parse("2023-01-01T00:00:00.00Z");
  private static final Instant januaryLastInstant = Instant.parse("2023-01-31T00:00:00.00Z");

  private static final ZoneId zoneId = Clock.systemDefaultZone().getZone();
  private static final Clock clock = Clock.fixed(tuesdayInstant, zoneId);

  private static final ZonedDateTime mondayMidnight =
          ZonedDateTime.ofInstant(mondayInstant, zoneId);
  private static final ZonedDateTime tuesdayMidnight =
          ZonedDateTime.ofInstant(tuesdayInstant, zoneId);
  private static final ZonedDateTime sundayMidnight =
          ZonedDateTime.ofInstant(sundayInstant, zoneId);
  private static final ZonedDateTime januaryFirst =
          ZonedDateTime.ofInstant(januaryFirstInstant, zoneId);
  private static final ZonedDateTime januaryLast =
          ZonedDateTime.ofInstant(januaryLastInstant, zoneId);

  private PeriodGenerator periodGenerator;

  @Before
  public void setUp() {
    periodGenerator = new PeriodGenerator(clock);
  }

  @Test
  public void shouldGenerateCorrectDailyTimeRange() {
    Pair<ZonedDateTime, ZonedDateTime> range =
            periodGenerator.generateRange(DhisPeriods.DAILY, (long) 90);

    ZonedDateTime dayStart = tuesdayMidnight.with(LocalTime.of(1, 30));
    ZonedDateTime dayEnd = tuesdayMidnight.with(LocalTime.of(23, 59));

    assertThat(range.getFirst(), is(dayStart));
    assertThat(range.getSecond(), is(dayEnd));
  }

  @Test
  public void shouldGenerateCorrectWeeklyTimeRange() {
    Pair<ZonedDateTime, ZonedDateTime> range =
            periodGenerator.generateRange(DhisPeriods.WEEKLY_MONDAY, (long) 90);

    ZonedDateTime weekStart = mondayMidnight.with(LocalTime.of(1, 30));
    ZonedDateTime weekEnd = sundayMidnight.with(LocalTime.of(23, 59));

    assertThat(range.getFirst(), is(weekStart));
    assertThat(range.getSecond(), is(weekEnd));
  }

  @Test
  public void shouldGenerateCorrectMonthlyTimeRange() {
    Pair<ZonedDateTime, ZonedDateTime> range =
            periodGenerator.generateRange(DhisPeriods.MONTHLY, (long) 90);

    ZonedDateTime monthStart = januaryFirst.with(LocalTime.of(1, 30));
    ZonedDateTime monthEnd = januaryLast.with(LocalTime.of(23, 59));

    assertThat(range.getFirst(), is(monthStart));
    assertThat(range.getSecond(), is(monthEnd));
  }

}
