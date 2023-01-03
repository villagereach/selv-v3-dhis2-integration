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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AuthService {

  static final String API_TOKEN_URL = "/api/apiToken";
  static final String RESPONSE = "response";
  static final String KEY = "key";

  private final RestOperations restTemplate;

  public AuthService() {
    this(new RestTemplate());
  }

  public AuthService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Retrieves access token from the auth service.
   *
   * @param username  Name of the specific user.
   * @param password  User password.
   * @param serverUrl User's server address.
   * @return token.
   */
  public String obtainAccessToken(String username, String password, String serverUrl) {

    String plainCreds = username + ":" + password;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);

    Map<String, String> emptyRequestBody = new HashMap<>();

    HttpEntity<?> request = new HttpEntity<>(emptyRequestBody, headers);

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().uri(URI.create(
            serverUrl + API_TOKEN_URL
    ));
    URI uri = uriBuilder.build(true).toUri();

    ResponseEntity<?> response = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            request,
            Object.class);

    return ((Map<String, Map<String, String>>) response.getBody()).get(RESPONSE).get(KEY);
  }

}
