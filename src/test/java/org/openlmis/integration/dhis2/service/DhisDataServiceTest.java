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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.integration.dhis2.dto.dhis.DhisDataset;
import org.openlmis.integration.dhis2.exception.RestOperationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class DhisDataServiceTest {

  private static final String SERVER_URL = "https://play.dhis2.org/2.39.0.1";
  private static final String USERNAME = "username";
  private static final String PASSWORD = "p@ssw0rd";
  private static final String DATASET_ID = "dataset-id";

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private AuthService authService;

  @InjectMocks
  private DhisDataService dhisDataService;

  @Before
  public void setUp() {
    final String token = "r4nd0m70k3n";
    when(authService.obtainAccessToken(anyString(), anyString(), anyString())).thenReturn(token);
  }

  @Test
  public void shouldReturnDhisDataset() {
    ResponseEntity<DhisDataset> response = mock(ResponseEntity.class);
    final DhisDataset dhisDataset = mock(DhisDataset.class);

    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(DhisDataset.class))
    ).thenReturn(response);

    when(response.getBody()).thenReturn(dhisDataset);

    DhisDataset newDhisDataset = dhisDataService.getDhisDataSetById(DATASET_ID, SERVER_URL,
            USERNAME, PASSWORD);
    assertThat(newDhisDataset, is(equalTo(dhisDataset)));
  }

  @Test(expected = RestOperationException.class)
  public void shouldThrowNotFoundException() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(DhisDataset.class))
    ).thenThrow(HttpClientErrorException.class);

    dhisDataService.getDhisDataSetById(DATASET_ID, SERVER_URL,
            USERNAME, PASSWORD);
  }

}
