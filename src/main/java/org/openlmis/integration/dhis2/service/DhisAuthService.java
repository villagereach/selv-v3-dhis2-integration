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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.openlmis.integration.dhis2.exception.ResponseParsingException;
import org.openlmis.integration.dhis2.exception.RestOperationException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class DhisAuthService {

  public static final String API_AUTH_URL = "/api/apiToken";
  public static final String API_RESPONSE_DETAILS = "response";
  public static final String API_KEY = "key";

  @Autowired
  private RestTemplate restTemplate;

  /**
   * Retrieves access token from the auth service.
   *
   * @param username  Name of the specific user.
   * @param password  User password.
   * @param serverUrl User's server address.
   * @return token.
   */
  public String obtainAccessToken(String username, String password, String serverUrl) {
    String base64Creds = getEncodedUserCreds(username, password);
    String uri = serverUrl + API_AUTH_URL;

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);

    Map<String, String> emptyRequestBody = new HashMap<>();

    HttpEntity<?> request = new HttpEntity<>(emptyRequestBody, headers);

    ResponseEntity<?> response;

    try {
      response = restTemplate.exchange(
              uri,
              HttpMethod.POST,
              request,
              Object.class);
    } catch (RestClientException ex) {
      throw new RestOperationException(MessageKeys.ERROR_EXTERNAL_API_CONNECTION_FAILED, ex);
    }

    try {
      return ((Map<String, Map<String, String>>) response.getBody()).get(API_RESPONSE_DETAILS)
              .get(API_KEY);
    } catch (NullPointerException ex) {
      throw new ResponseParsingException(
              MessageKeys.ERROR_EXTERNAL_API_RESPONSE_BODY_UNABLE_TO_PARSE, ex);
    }
  }

  private String getEncodedUserCreds(String username, String password) {
    String plainCreds = username + ":" + password;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    return new String(base64CredsBytes);
  }

}
