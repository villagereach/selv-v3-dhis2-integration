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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.integration.dhis2.service.communication.ReferenceDataService.API_URL;
import static org.openlmis.integration.dhis2.service.communication.ReferenceDataService.FACILITIES_RESOURCE_PATH;
import static org.openlmis.integration.dhis2.service.communication.ReferenceDataService.ORDERABLES_RESOURCE_PATH;
import static org.openlmis.integration.dhis2.service.communication.ReferenceDataService.PROCESSING_PERIODS_RESOURCE_PATH;

import java.net.URI;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.integration.dhis2.dto.referencedata.MinimalFacilityDto;
import org.openlmis.integration.dhis2.dto.referencedata.OrderableDto;
import org.openlmis.integration.dhis2.dto.referencedata.PageDto;
import org.openlmis.integration.dhis2.dto.referencedata.ProcessingPeriodDto;
import org.openlmis.integration.dhis2.exception.RestOperationException;
import org.openlmis.integration.dhis2.service.auth.ReferenceDataAuthService;
import org.openlmis.integration.dhis2.service.communication.ReferenceDataService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataServiceTest {

  private static final String TOKEN = "4u7h-70k3n";
  private static final String SERVICE_URL = "http://localhost";

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private ReferenceDataAuthService authService;

  @InjectMocks
  private ReferenceDataService referenceDataService;

  @Before
  public void setUp() throws Exception {
    ReflectionTestUtils.setField(referenceDataService, "serviceUrl", SERVICE_URL);
  }

  @Test
  public void shouldReturnPageOfMinimalFacilityDtos() {
    final List<MinimalFacilityDto> dtos = (List<MinimalFacilityDto>) mock(List.class);
    final PageDto<MinimalFacilityDto> minimalFacilityDtos = createPageDto(dtos);
    ResponseEntity<PageDto<MinimalFacilityDto>> response =
            new ResponseEntity<>(minimalFacilityDtos, HttpStatus.OK);
    URI uri = URI.create(SERVICE_URL + API_URL + FACILITIES_RESOURCE_PATH);

    when(restTemplate.exchange(eq(uri),
            eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
    )).thenReturn(response);
    when(authService.obtainAccessToken()).thenReturn(TOKEN);
    PageDto<MinimalFacilityDto> result = referenceDataService.findAllFacilities();

    assertThat(result, is(equalTo(minimalFacilityDtos)));
  }

  @Test
  public void shouldReturnPageOfOrderableDtos() {
    final List<OrderableDto> dtos = (List<OrderableDto>) mock(List.class);
    final PageDto<OrderableDto> orderableDtos = createPageDto(dtos);
    ResponseEntity<PageDto<OrderableDto>> response =
            new ResponseEntity<>(orderableDtos, HttpStatus.OK);
    URI uri = URI.create(SERVICE_URL + API_URL + ORDERABLES_RESOURCE_PATH);

    when(restTemplate.exchange(eq(uri),
            eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
    )).thenReturn(response);
    when(authService.obtainAccessToken()).thenReturn(TOKEN);
    PageDto<OrderableDto> result = referenceDataService.findAllOrderables();

    assertThat(result, is(equalTo(orderableDtos)));
  }

  @Test
  public void shouldReturnPageOfProcessingPeriodDtos() {
    final List<ProcessingPeriodDto> dtos = (List<ProcessingPeriodDto>) mock(List.class);
    final PageDto<ProcessingPeriodDto> processingPeriodDtos = createPageDto(dtos);
    ResponseEntity<PageDto<ProcessingPeriodDto>> response =
            new ResponseEntity<>(processingPeriodDtos, HttpStatus.OK);
    URI uri = URI.create(SERVICE_URL + API_URL + PROCESSING_PERIODS_RESOURCE_PATH);

    when(restTemplate.exchange(eq(uri),
            eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
    )).thenReturn(response);
    when(authService.obtainAccessToken()).thenReturn(TOKEN);
    PageDto<ProcessingPeriodDto> result = referenceDataService.findAllProcessingPeriods();

    assertThat(result, is(equalTo(processingPeriodDtos)));
  }

  @Test(expected = RestOperationException.class)
  public void shouldThrowNotFoundException() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
    )).thenThrow(HttpClientErrorException.class);

    referenceDataService.findAllOrderables();
  }

  private <T> PageDto<T> createPageDto(List<T> content) {
    return new PageDto<T>(false, false, 1, 1L, 1, 1, 1, Sort.by("sort-order"), content);
  }

}