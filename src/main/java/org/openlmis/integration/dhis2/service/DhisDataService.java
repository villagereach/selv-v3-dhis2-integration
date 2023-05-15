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
import static org.openlmis.integration.dhis2.util.RequestHelper.createUri;

import java.util.List;
import java.util.Map;
import org.openlmis.integration.dhis2.dto.dhis.DataValueSet;
import org.openlmis.integration.dhis2.dto.dhis.DhisCategoryOptionCombo;
import org.openlmis.integration.dhis2.dto.dhis.DhisCategoryOptionComboResponseBody;
import org.openlmis.integration.dhis2.dto.dhis.DhisDataset;
import org.openlmis.integration.dhis2.dto.dhis.DhisResponseBody;
import org.openlmis.integration.dhis2.dto.dhis.OrganisationUnit;
import org.openlmis.integration.dhis2.dto.dhis.OrganisationUnitResponseBody;
import org.openlmis.integration.dhis2.exception.ResponseParsingException;
import org.openlmis.integration.dhis2.exception.RestOperationException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.service.auth.DhisAuthService;
import org.openlmis.integration.dhis2.util.RequestParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class DhisDataService {

  public static final String API_DATASETS_URL = "/api/dataSets";
  public static final String API_DATA_VALUE_SETS_URL = "/api/dataValueSets";
  public static final String API_ORG_UNITS_URL = "/api/organisationUnits";
  public static final String API_CATEGORY_OPTION_COMBOS_URL = "/api/categoryOptionCombos";
  public static final String API_TOKEN = "ApiToken";

  @Autowired
  private DhisAuthService authService;

  @Autowired
  private RestTemplate restTemplate;

  /**
   * Get data set with specific ID from DHIS2 API.
   *
   * @param id        ID of dataset to get.
   * @param serverUrl Url of the dhis2 server.
   * @param username  Name of the specific user.
   * @param password  User password.
   * @return the {@link DhisDataset} with specific ID.
   */
  public DhisDataset getDhisDataSetById(String id, String serverUrl, String username,
      String password) {
    String token = authService.obtainAccessToken(username, password, serverUrl);

    RequestParameters params = RequestParameters
        .init()
        .set("fields", "periodType,"
            + "organisationUnits[id,code,name],"
            + "dataSetElements[dataElement[id,name]]");

    try {
      ResponseEntity<DhisDataset> response = restTemplate.exchange(
          createUri(serverUrl + API_DATASETS_URL + "/" + id, params),
          HttpMethod.GET,
          createEntity(token, API_TOKEN),
          DhisDataset.class
      );

      return response.getBody();
    } catch (HttpClientErrorException ex) {
      throw new RestOperationException(
          MessageKeys.ERROR_EXTERNAL_API_CLIENT_REQUEST_FAILED, ex);
    } catch (RestClientException ex) {
      throw new RestOperationException(MessageKeys.ERROR_EXTERNAL_API_CONNECTION_FAILED, ex);
    }
  }

  /**
   * Get all datasets for a given server from DHIS2 API.
   *
   * @param serverUrl Url of the dhis2 server.
   * @param username  Name of the specific user.
   * @param password  User password.
   * @return the {@link DhisDataset} list.
   */
  public List<DhisDataset> getDhisDatasets(String serverUrl, String username,
      String password) {
    String token = authService.obtainAccessToken(username, password, serverUrl);

    RequestParameters params = RequestParameters
        .init()
        .set("paging", "false");

    try {
      ResponseEntity<Map<String, List<Object>>> response = restTemplate.exchange(
          createUri(serverUrl + API_DATASETS_URL, params),
          HttpMethod.GET,
          createEntity(token, API_TOKEN),
          new ParameterizedTypeReference<Map<String, List<Object>>>() {}
      );

      List<?> datasets
          = response.getBody().get("dataSets");
      return (List<DhisDataset>) datasets;
    } catch (HttpClientErrorException ex) {
      throw new RestOperationException(
          MessageKeys.ERROR_EXTERNAL_API_CLIENT_REQUEST_FAILED, ex);
    } catch (RestClientException ex) {
      throw new RestOperationException(MessageKeys.ERROR_EXTERNAL_API_CONNECTION_FAILED, ex);
    } catch (NullPointerException ex) {
      throw new ResponseParsingException(
          MessageKeys.ERROR_EXTERNAL_API_RESPONSE_BODY_UNABLE_TO_PARSE, ex);
    }
  }

  /**
   * Get all organisational units for a given server from DHIS2 API.
   *
   * @param serverUrl Url of the dhis2 server.
   * @param username  Name of the specific user.
   * @param password  User password.
   * @return the {@link OrganisationUnit} list.
   */
  public List<OrganisationUnit> getDhisOrgUnits(String serverUrl, String username,
      String password) {
    String token = authService.obtainAccessToken(username, password, serverUrl);

    RequestParameters params = RequestParameters
        .init()
        .set("fields", "id,name,code");

    try {
      ResponseEntity<OrganisationUnitResponseBody> response = restTemplate.exchange(
          createUri(serverUrl + API_ORG_UNITS_URL, params),
          HttpMethod.GET,
          createEntity(token, API_TOKEN),
          new ParameterizedTypeReference<OrganisationUnitResponseBody>() {}
      );

      return response.getBody().getOrganisationUnits();
    } catch (HttpClientErrorException ex) {
      throw new RestOperationException(
          MessageKeys.ERROR_EXTERNAL_API_CLIENT_REQUEST_FAILED, ex);
    } catch (RestClientException ex) {
      throw new RestOperationException(MessageKeys.ERROR_EXTERNAL_API_CONNECTION_FAILED, ex);
    } catch (NullPointerException ex) {
      throw new ResponseParsingException(
          MessageKeys.ERROR_EXTERNAL_API_RESPONSE_BODY_UNABLE_TO_PARSE, ex);
    }
  }

  /**
   * Get all category option combos from DHIS2 API.
   *
   * @param serverUrl Url of the dhis2 server.
   * @param username  Name of the specific user.
   * @param password  User password.
   * @return the {@link DhisCategoryOptionCombo} list.
   */
  public List<DhisCategoryOptionCombo> getDhisCategoryOptionCombos(String serverUrl,
      String username, String password) {
    String token = authService.obtainAccessToken(username, password, serverUrl);
    RequestParameters params = RequestParameters
        .init();

    try {
      ResponseEntity<DhisCategoryOptionComboResponseBody> response = restTemplate.exchange(
          createUri(serverUrl + API_CATEGORY_OPTION_COMBOS_URL, params),
          HttpMethod.GET,
          createEntity(token, API_TOKEN),
          new ParameterizedTypeReference<DhisCategoryOptionComboResponseBody>() {}
      );

      return response.getBody().getCategoryOptionCombos();
    } catch (HttpClientErrorException ex) {
      throw new RestOperationException(
          MessageKeys.ERROR_EXTERNAL_API_CLIENT_REQUEST_FAILED, ex);
    } catch (RestClientException ex) {
      throw new RestOperationException(MessageKeys.ERROR_EXTERNAL_API_CONNECTION_FAILED, ex);
    } catch (NullPointerException ex) {
      throw new ResponseParsingException(
          MessageKeys.ERROR_EXTERNAL_API_RESPONSE_BODY_UNABLE_TO_PARSE, ex);
    }
  }

  /**
   * Send {@link DataValueSet} to DHIS2 API.
   *
   * @param dataValueSet Request's payload send to DHIS2 API.
   * @param serverUrl    Url of the dhis2 server.
   * @param username     Name of the specific user.
   * @param password     User password.
   * @return the {@link DhisResponseBody}
   */
  public DhisResponseBody createDataValueSet(DataValueSet dataValueSet, String serverUrl,
      String username, String password) {
    String token = authService.obtainAccessToken(username, password, serverUrl);

    RequestParameters params = RequestParameters
        .init()
        .set("orgUnitIdScheme", "code")
        .set("dataElementIdScheme", "name");

    try {
      ResponseEntity<DhisResponseBody> response = restTemplate.exchange(
          createUri(serverUrl + API_DATA_VALUE_SETS_URL, params),
          HttpMethod.POST,
          createEntity(dataValueSet, token, API_TOKEN),
          DhisResponseBody.class
      );

      return response.getBody();
    } catch (HttpClientErrorException ex) {
      throw new RestOperationException(
          MessageKeys.ERROR_EXTERNAL_API_CLIENT_REQUEST_FAILED, ex);
    } catch (RestClientException ex) {
      throw new RestOperationException(MessageKeys.ERROR_EXTERNAL_API_CONNECTION_FAILED, ex);
    }
  }

}
