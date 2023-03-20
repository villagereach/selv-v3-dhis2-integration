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

import java.time.ZonedDateTime;
import java.util.Date;
import org.openlmis.integration.dhis2.domain.enumerator.DhisPeriod;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

public class CronTrigger implements Trigger {

  private final PeriodGeneratorService periodGeneratorService;

  private final DhisPeriod periodEnum;
  private final int offsetMinutes;

  /**
   * Creates new instance of cron trigger.
   */
  public CronTrigger(PeriodGeneratorService periodGeneratorService,
                     DhisPeriod periodEnum, int offsetMinutes) {
    this.periodGeneratorService = periodGeneratorService;
    this.periodEnum = periodEnum;
    this.offsetMinutes = offsetMinutes;
  }

  @Override
  public Date nextExecutionTime(@Nullable TriggerContext triggerContext) {
    Pair<ZonedDateTime, ZonedDateTime> range = periodGeneratorService.generateRange(
            periodEnum, offsetMinutes);
    ZonedDateTime periodEnd = range.getSecond();
    return Date.from(periodEnd.toInstant());
  }

}
