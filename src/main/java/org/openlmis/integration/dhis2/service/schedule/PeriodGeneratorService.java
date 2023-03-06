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

package org.openlmis.integration.dhis2.service.schedule;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.openlmis.integration.dhis2.domain.enumerator.DhisPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class PeriodGeneratorService {

  @Autowired
  private Clock clock;

  public PeriodGeneratorService() {
  }

  public PeriodGeneratorService(Clock clock) {
    this.clock = clock;
  }

  /**
   * Generates date range between starting and ending period based on given enumerator.
   *
   * @param periodEnum Enumerator with period name
   * @param offsetMinutes Starting date offset in minutes
   * @return Pair of starting and end date
   */
  public Pair<ZonedDateTime, ZonedDateTime> generateRange(
          DhisPeriod periodEnum, int offsetMinutes) {
    Pair<ZonedDateTime, ZonedDateTime> range =
            periodEnum.generate(ZonedDateTime.now(this.clock));
    return Pair.of(range.getFirst().plusMinutes(offsetMinutes), range.getSecond());
  }

  public Pair<ZonedDateTime, ZonedDateTime> generateRange(
          String periodName, int offsetMinutes) {
    return generateRange(fromString(periodName), offsetMinutes);
  }

  /**
   * Create enumerator from string.
   */
  public DhisPeriod fromString(String name) {
    try {
      return DhisPeriod.valueOf(name.replaceAll(" ", "_").toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(String.format(
              "There is no enumerator with name '%s'", name), ex);
    }
  }

  /**
   * Format date to ISO format used by DHIS2.
   */
  public String formatDate(ZonedDateTime date, DhisPeriod periodEnum) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
            DhisPeriod.getIsoPattern(periodEnum));
    return date.format(dateTimeFormatter);
  }

  public String formatDate(ZonedDateTime date, String periodEnum) {
    return formatDate(date, fromString(periodEnum));
  }

}
