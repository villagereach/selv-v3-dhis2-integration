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

package org.openlmis.integration.dhis2.util.messagekeys;

import java.util.Arrays;

public abstract class MessageKeys {

  private static final String DELIMITER = ".";

  // General
  private static final String SERVICE = "dhis2-integration";
  protected static final String SERVICE_ERROR = join(SERVICE, "error");

  protected static final String EXTERNAL = "external";
  protected static final String FAILED = "failed";
  protected static final String CONNECTION = "connection";

  // Entities
  protected static final String AUTH = "auth";
  protected static final String API = "api";

  protected static final String UNABLE_TO_PARSE = "unableToParse";

  // Common to subclasses
  protected static final String RESPONSE = "response";

  protected MessageKeys() {
    throw new UnsupportedOperationException();
  }

  protected static String join(String... params) {
    return String.join(DELIMITER, Arrays.asList(params));
  }
}
