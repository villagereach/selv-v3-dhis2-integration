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

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.openlmis.integration.dhis2.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.integration.dhis2.i18n.MessageKeys.ERROR_PERMISSION_CHECK_FAILED;

import java.util.UUID;
import org.openlmis.integration.dhis2.dto.referencedata.ResultDto;
import org.openlmis.integration.dhis2.dto.referencedata.RightDto;
import org.openlmis.integration.dhis2.dto.referencedata.UserDto;
import org.openlmis.integration.dhis2.exception.PermissionMessageException;
import org.openlmis.integration.dhis2.service.referencedata.UserReferenceDataService;
import org.openlmis.integration.dhis2.util.AuthenticationHelper;
import org.openlmis.integration.dhis2.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class PermissionService {

  public static final String DHIS2_ADMIN = "DHIS2_ADMIN";

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private PermissionStrings permissionStrings;

  @Value("${auth.server.clientId}")
  private String serviceTokenClientId;

  @Value("${auth.server.clientId.apiKey.prefix}")
  private String apiKeyPrefix;

  /**
   * Checks if current user has permission to manage DHIS2 integration.
   */
  public void canManageDhisIntegration() {
    hasPermission(DHIS2_ADMIN);
  }

  public PermissionStrings.Handler getPermissionStrings(UUID userId) {
    return permissionStrings.forUser(userId);
  }

  private void hasPermission(String rightName) {
    ResultDto<Boolean> result = getRightResult(rightName, null, null, null, false);
    if (null == result || !result.getResult()) {
      throw new PermissionMessageException(
              new Message(ERROR_NO_FOLLOWING_PERMISSION, rightName));
    }
  }

  private ResultDto<Boolean> getRightResult(String rightName, UUID program, UUID facility,
                                            UUID warehouse, boolean allowApiKey) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
            .getContext()
            .getAuthentication();

    return authentication.isClientOnly()
            ? checkServiceToken(allowApiKey, authentication)
            : checkUserToken(rightName, program, facility, warehouse);
  }

  private ResultDto<Boolean> checkUserToken(String rightName, UUID program, UUID facility,
                                            UUID warehouse) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);

    try {
      return userReferenceDataService.hasRight(
              user.getId(), right.getId(), program, facility, warehouse);
    } catch (HttpClientErrorException httpException) {
      throw new PermissionMessageException(new Message(ERROR_PERMISSION_CHECK_FAILED,
              httpException.getMessage()), httpException);
    }
  }

  private ResultDto<Boolean> checkServiceToken(boolean allowApiKey,
                                               OAuth2Authentication authentication) {
    String clientId = authentication.getOAuth2Request().getClientId();

    if (serviceTokenClientId.equals(clientId)) {
      return new ResultDto<>(true);
    }

    if (startsWith(clientId, apiKeyPrefix)) {
      return new ResultDto<>(allowApiKey);
    }

    return new ResultDto<>(false);
  }

}
