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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.integration.dhis2.dto.dhis.DataValueSet;
import org.openlmis.integration.dhis2.dto.dhis.DhisCategoryOptionCombo;
import org.openlmis.integration.dhis2.dto.dhis.DhisCategoryOptionComboResponseBody;
import org.openlmis.integration.dhis2.dto.dhis.DhisDataset;
import org.openlmis.integration.dhis2.dto.dhis.DhisPeriodType;
import org.openlmis.integration.dhis2.dto.dhis.DhisPeriodTypeResponseBody;
import org.openlmis.integration.dhis2.dto.dhis.DhisResponseBody;
import org.openlmis.integration.dhis2.exception.RestOperationException;
import org.openlmis.integration.dhis2.service.auth.DhisAuthService;
import org.openlmis.integration.dhis2.service.communication.DhisDataService;
import org.springframework.core.ParameterizedTypeReference;
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
  private DataValueSet dataValueSet;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private DhisAuthService authService;

  @InjectMocks
  private DhisDataService dhisDataService;

  @Before
  public void setUp() {
    final String token = "r4nd0m70k3n";
    dataValueSet = mock(DataValueSet.class);
    when(authService.obtainAccessToken(anyString(), anyString(), anyString())).thenReturn(token);
  }

  @Test
  public void getDhisDataSetByIdShouldReturnDhisDataset() {
    final ResponseEntity<DhisDataset> response = mock(ResponseEntity.class);
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
  public void getDhisDataSetByIdShouldThrowNotFoundException() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(DhisDataset.class))
    ).thenThrow(HttpClientErrorException.class);

    dhisDataService.getDhisDataSetById(DATASET_ID, SERVER_URL, USERNAME, PASSWORD);
  }

  @Test
  public void getDhisDatasetsShouldReturnDhisDatasetList() {
    final ResponseEntity<Map<String, List<Object>>> response =
            mock(ResponseEntity.class);
    final Map<String, List<Object>> body =
            mock(LinkedHashMap.class);
    final List<Object> datasets = new ArrayList<>(
            Arrays.asList(mock(DhisDataset.class), mock(DhisDataset.class)));

    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class))
    ).thenReturn(response);

    when(response.getBody()).thenReturn(body);
    when(body.get("dataSets")).thenReturn(datasets);

    List<DhisDataset> newDhisDatasets = dhisDataService.getDhisDatasets(SERVER_URL,
            USERNAME, PASSWORD);
    assertThat(newDhisDatasets, is(equalTo(datasets)));
  }

  @Test(expected = RestOperationException.class)
  public void getDhisDatasetsShouldThrowNotFoundException() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, List<Object>>>() {
            }))
    ).thenThrow(HttpClientErrorException.class);

    dhisDataService.getDhisDatasets(SERVER_URL, USERNAME, PASSWORD);
  }

  @Test
  public void getDhisCategoryOptionCombosShouldReturnCategoryOptionComboList() {
    final ResponseEntity<DhisCategoryOptionComboResponseBody> response = mock(ResponseEntity.class);
    final DhisCategoryOptionComboResponseBody categoryOptionComboResponseBody
            = mock(DhisCategoryOptionComboResponseBody.class);
    final List<DhisCategoryOptionCombo> categoryOptionCombos = new ArrayList<>(
            Arrays.asList(mock(DhisCategoryOptionCombo.class),
                    mock(DhisCategoryOptionCombo.class)));

    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(new ParameterizedTypeReference<DhisCategoryOptionComboResponseBody>() {
            }))
    ).thenReturn(response);

    when(response.getBody()).thenReturn(categoryOptionComboResponseBody);
    when(categoryOptionComboResponseBody.getCategoryOptionCombos())
            .thenReturn(categoryOptionCombos);

    List<DhisCategoryOptionCombo> newCategoryOptionCombos =
            dhisDataService.getDhisCategoryOptionCombos(SERVER_URL, USERNAME, PASSWORD);
    assertThat(newCategoryOptionCombos, is(equalTo(categoryOptionCombos)));
  }

  @Test(expected = RestOperationException.class)
  public void getDhisCategoryOptionCombosShouldThrowNotFoundException() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(new ParameterizedTypeReference<DhisCategoryOptionComboResponseBody>() {
            }))
    ).thenThrow(HttpClientErrorException.class);

    dhisDataService.getDhisCategoryOptionCombos(SERVER_URL, USERNAME, PASSWORD);
  }

  @Test
  public void createDataValueSetShouldReturnDhisResponseBody() {
    final ResponseEntity<DhisResponseBody> response = mock(ResponseEntity.class);
    final DhisResponseBody dhisResponseBody = mock(DhisResponseBody.class);

    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
            eq(DhisResponseBody.class))
    ).thenReturn(response);

    when(response.getBody()).thenReturn(dhisResponseBody);

    DhisResponseBody newDhisResponseBody = dhisDataService.sendDataValueSet(dataValueSet,
            SERVER_URL, USERNAME, PASSWORD);
    assertThat(newDhisResponseBody, is(equalTo(dhisResponseBody)));
  }

  @Test(expected = RestOperationException.class)
  public void createDataValueSetShouldThrowNotFoundException() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
            eq(DhisResponseBody.class))
    ).thenThrow(HttpClientErrorException.class);

    dhisDataService.sendDataValueSet(dataValueSet, SERVER_URL, USERNAME, PASSWORD);
  }

  @Test
  public void getDhisPeriodTypesShouldReturnPeriodTypeList() {
    ResponseEntity<DhisPeriodTypeResponseBody> response = mock(ResponseEntity.class);
    DhisPeriodTypeResponseBody dhisPeriodTypeResponseBody = new DhisPeriodTypeResponseBody();
    List<DhisPeriodType> periodTypes = new ArrayList<>();
    dhisPeriodTypeResponseBody.setPeriodTypes(periodTypes);

    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(new ParameterizedTypeReference<DhisPeriodTypeResponseBody>() {}))
    ).thenReturn(response);
    when(response.getBody()).thenReturn(dhisPeriodTypeResponseBody);

    List<DhisPeriodType> result =
            dhisDataService.getDhisPeriodTypes(SERVER_URL, USERNAME, PASSWORD);
    assertThat(result, is(equalTo(periodTypes)));
  }

  @Test(expected = RestOperationException.class)
  public void getDhisPeriodTypesShouldThrowNotFoundException() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(new ParameterizedTypeReference<DhisPeriodTypeResponseBody>() {}))
    ).thenThrow(HttpClientErrorException.class);

    dhisDataService.getDhisPeriodTypes(SERVER_URL, USERNAME, PASSWORD);
  }

}
