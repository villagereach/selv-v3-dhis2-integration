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

import static org.openlmis.integration.dhis2.util.RequestHelper.createEntity;

import java.net.URI;
import org.openlmis.integration.dhis2.dto.referencedata.MinimalFacilityDto;
import org.openlmis.integration.dhis2.dto.referencedata.OrderableDto;
import org.openlmis.integration.dhis2.dto.referencedata.PageDto;
import org.openlmis.integration.dhis2.exception.ResponseParsingException;
import org.openlmis.integration.dhis2.exception.RestOperationException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.service.auth.ReferenceDataAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ReferenceDataService {

  public static final String API_URL = "/api/";
  public static final String FACILITIES_RESOUCE_PATH = "facilities/";
  public static final String ORDERABLES_RESOUCE_PATH = "orderables/";

  @Value("${service.url}")
  private String serviceUrl;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ReferenceDataAuthService authService;

  /**
   * Retrieves {@link PageDto} of {@link MinimalFacilityDto} objects from referencedata service.
   *
   * @return page of MinimalFacilityDto objects.
   */
  public PageDto<MinimalFacilityDto> findAllFacilities() {
    return doRequest(FACILITIES_RESOUCE_PATH);
  }

  /**
   * Retrieves {@link PageDto} of {@link OrderableDto} objects from referencedata service.
   *
   * @return page of OrderableDto objects.
   */
  public PageDto<OrderableDto> findAllOrderables() {
    return doRequest(ORDERABLES_RESOUCE_PATH);
  }

  private <T> PageDto<T> doRequest(String resourcePath) {
    try {
      ResponseEntity<PageDto<T>> response = restTemplate.exchange(
              URI.create(serviceUrl + API_URL + resourcePath),
              HttpMethod.GET,
              createEntity(authService.obtainAccessToken(), "Bearer"),
              new ParameterizedTypeReference<PageDto<T>>() {}
      );

      try {
        return response.getBody();
      } catch (NullPointerException ex) {
        throw new ResponseParsingException(
                MessageKeys.ERROR_EXTERNAL_API_RESPONSE_BODY_UNABLE_TO_PARSE, ex);
      }
    } catch (HttpClientErrorException ex) {
      throw new RestOperationException(
              MessageKeys.ERROR_EXTERNAL_API_CLIENT_REQUEST_FAILED, ex);
    } catch (RestClientException ex) {
      throw new RestOperationException(MessageKeys.ERROR_EXTERNAL_API_CONNECTION_FAILED, ex);
    }
  }

}
