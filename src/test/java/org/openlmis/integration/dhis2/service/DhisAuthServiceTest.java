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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.integration.dhis2.exception.RestOperationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class DhisAuthServiceTest {

  private static final String SERVER_URL = "https://play.dhis2.org/2.39.0.1";
  private static final String AUTHORIZATION_URL = SERVER_URL + DhisAuthService.API_AUTH_URL;
  private static final String USERNAME = "username";
  private static final String PASSWORD = "p@ssw0rd";

  @Mock
  private RestTemplate restTemplate;

  @Captor
  private ArgumentCaptor<HttpEntity<String>> entityStringCaptor;

  @InjectMocks
  private DhisAuthService authService;

  @Test
  public void shouldObtainAccessToken() {
    final String token = "r4nd0m70k3n";

    ResponseEntity<Object> response = mock(ResponseEntity.class);
    Map<String, String> tokenBody = ImmutableMap.of(DhisAuthService.API_KEY, token);
    Map<String, Map> body = ImmutableMap.of(DhisAuthService.API_RESPONSE_DETAILS, tokenBody);

    when(restTemplate.exchange(
            eq(AUTHORIZATION_URL), eq(HttpMethod.POST),
            any(HttpEntity.class), eq(Object.class)
    )).thenReturn(response);

    when(response.getBody()).thenReturn(body);

    String obtainedToken = authService.obtainAccessToken(USERNAME, PASSWORD, SERVER_URL);
    assertThat(obtainedToken, is(equalTo(token)));

    verify(restTemplate).exchange(
            eq(AUTHORIZATION_URL), eq(HttpMethod.POST),
            entityStringCaptor.capture(), eq(Object.class)
    );

    HttpEntity<String> entity = entityStringCaptor.getValue();
    Pattern pattern = Pattern.compile("^Basic (.*)");
    assertThat(
            entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
            hasItem(matchesPattern(pattern))
    );
  }

  @Test(expected = RestOperationException.class)
  public void shouldThrowRestOperationException() {
    when(restTemplate.exchange(
            eq(AUTHORIZATION_URL), eq(HttpMethod.POST),
            any(HttpEntity.class), eq(Object.class)
    )).thenThrow(RestClientException.class);

    String obtainedToken = authService.obtainAccessToken(USERNAME, PASSWORD, SERVER_URL);
    assertNull(obtainedToken);
  }

}
