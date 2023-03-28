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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.openlmis.integration.dhis2.domain.enumerator.DhisPeriod;
import org.openlmis.integration.dhis2.dto.referencedata.ProcessingPeriodDto;
import org.openlmis.integration.dhis2.exception.ValidationMessageException;
import org.openlmis.integration.dhis2.service.ReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class PeriodGeneratorService {

  @Autowired
  private Clock clock;

  @Autowired
  private ReferenceDataService referenceDataService;

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
   * Retrieves last finished requisition period from Requisition service.
   */
  public Pair<ZonedDateTime, ZonedDateTime> getLastRequisitionPeriod() {
    Date currentDate = Date.from(ZonedDateTime.now().toInstant());
    List<ProcessingPeriodDto> processingPeriods =
            referenceDataService.findAllProcessingPeriods().getContent();

    processingPeriods.sort(Comparator.comparing(ProcessingPeriodDto::getStartDate));
    Optional<ProcessingPeriodDto> lastPeriodOptional = processingPeriods.stream()
            .filter(e -> e.getEndDate().before(currentDate))
            .max(Comparator.comparing(ProcessingPeriodDto::getStartDate));

    ProcessingPeriodDto lastPeriod = lastPeriodOptional.orElseThrow(
        () -> new ValidationMessageException("No matching processing periods found"));
    ZonedDateTime startDate = ZonedDateTime.ofInstant(
            lastPeriod.getStartDate().toInstant(), ZoneId.systemDefault());
    ZonedDateTime endDate = ZonedDateTime.ofInstant(
            lastPeriod.getEndDate().toInstant(), ZoneId.systemDefault());

    return Pair.of(startDate, endDate);
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
