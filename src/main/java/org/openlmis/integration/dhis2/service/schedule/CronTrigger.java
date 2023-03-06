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

import java.util.Date;
import org.openlmis.integration.dhis2.domain.enumerator.DhisPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

public class CronTrigger implements Trigger {

  private final DhisPeriod periodEnum;
  private final int offsetMinutes;

  @Autowired
  private PeriodGeneratorService periodGeneratorService;

  public CronTrigger(DhisPeriod periodEnum, int offsetMinutes) {
    this.periodEnum = periodEnum;
    this.offsetMinutes = offsetMinutes;
  }

  @Override
  public Date nextExecutionTime(@Nullable TriggerContext triggerContext) {
    return Date.from(periodGeneratorService.generateRange(
            periodEnum, offsetMinutes).getSecond().toInstant());
  }

}
