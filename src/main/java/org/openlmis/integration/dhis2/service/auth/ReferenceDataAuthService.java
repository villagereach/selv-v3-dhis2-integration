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

package org.openlmis.integration.dhis2.service.auth;

import static org.openlmis.integration.dhis2.util.RequestHelper.createUri;

import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.openlmis.integration.dhis2.util.RequestParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class ReferenceDataAuthService {

  private static final String ACCESS_TOKEN = "access_token";

  @Value("${auth.server.clientId}")
  private String clientId;

  @Value("${auth.server.clientSecret}")
  private String clientSecret;

  @Value("${auth.server.authorizationUrl}")
  private String authorizationUrl;

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Retrieves access token from the auth service.
   *
   * @return token.
   */
  public String obtainAccessToken() {
    String base64Creds = getEncodedUserCreds(clientId, clientSecret);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + base64Creds);

    HttpEntity<String> request = new HttpEntity<>(headers);

    RequestParameters params = RequestParameters
        .init()
        .set("grant_type", "client_credentials");

    ResponseEntity<?> response = restTemplate.exchange(
        createUri(authorizationUrl, params), HttpMethod.POST, request, Object.class
    );

    return ((Map<String, String>) response.getBody()).get(ACCESS_TOKEN);
  }

  @CacheEvict(cacheNames = "token", allEntries = true)
  public void clearTokenCache() {
    // Intentionally blank
  }

  public void setRestTemplate(RestOperations restTemplate) {
    this.restTemplate = restTemplate;
  }

  private String getEncodedUserCreds(String clientId, String clientSecret) {
    String plainCreds = clientId + ":" + clientSecret;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    return new String(base64CredsBytes);
  }

}
