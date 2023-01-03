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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceTest {

  @Mock
  private RestTemplate restTemplate;

  @Captor
  private ArgumentCaptor<HttpEntity<String>> entityStringCaptor;

  private AuthService authService;

  @Before
  public void setUp() throws Exception {
    authService = new AuthService(restTemplate);
  }

  @Test
  public void shouldObtainAccessToken() {

    String username = "admin";
    String password = "district";
    String serverUrl = "https://play.dhis2.org/2.39.0.1";
    String token = UUID.randomUUID().toString();

    URI authorizationUrl = URI.create(serverUrl + AuthService.API_TOKEN_URL);

    ResponseEntity<Object> response = mock(ResponseEntity.class);
    Map<String, String> tokenBody = ImmutableMap.of(AuthService.KEY, token);
    Map<String, Map> body = ImmutableMap.of(AuthService.RESPONSE, tokenBody);

    when(restTemplate.exchange(
            eq(authorizationUrl), eq(HttpMethod.POST),
            any(HttpEntity.class), eq(Object.class)
    )).thenReturn(response);

    when(response.getBody()).thenReturn(body);

    String obtainedToken = authService.obtainAccessToken(username, password, serverUrl);
    assertThat(obtainedToken, is(equalTo(token)));

    verify(restTemplate).exchange(
            eq(authorizationUrl), eq(HttpMethod.POST),
            entityStringCaptor.capture(), eq(Object.class)
    );

    HttpEntity<String> entity = entityStringCaptor.getValue();
    Pattern pattern = Pattern.compile("^Basic (.*)");
    assertThat(
            entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
            hasItem(matchesPattern(pattern))
    );

  }

}