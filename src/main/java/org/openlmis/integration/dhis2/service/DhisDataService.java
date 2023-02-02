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

import org.openlmis.integration.dhis2.dto.dhis.DhisDataset;
import org.openlmis.integration.dhis2.exception.RestOperationException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.util.RequestParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class DhisDataService {

  public static final String API_DATASETS_URL = "/api/dataSets";

  @Autowired
  private AuthService authService;

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
              createEntity(token, "ApiToken"),
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

}
