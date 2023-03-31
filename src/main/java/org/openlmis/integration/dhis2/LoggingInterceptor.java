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

package org.openlmis.integration.dhis2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Configuration
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

  static Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

  @Override
  public ClientHttpResponse intercept(
          HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex) throws IOException {
    LOGGER.debug("Request body: {}", new String(reqBody, StandardCharsets.UTF_8));
    ClientHttpResponse response = ex.execute(req, reqBody);
    if (LOGGER.isDebugEnabled()) {
      InputStreamReader isr = new InputStreamReader(
              response.getBody(), StandardCharsets.UTF_8);
      String body = new BufferedReader(isr).lines()
              .collect(Collectors.joining());
      LOGGER.debug("Response body: {}", body);
    }
    return response;
  }

}
