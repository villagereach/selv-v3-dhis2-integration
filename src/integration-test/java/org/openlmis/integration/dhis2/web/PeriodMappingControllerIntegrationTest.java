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

package org.openlmis.integration.dhis2.web;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Matchers.any;
import static org.openlmis.integration.dhis2.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.javers.core.commit.CommitId;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.UnboundedValueObjectId;
import org.javers.repository.jql.JqlQuery;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.integration.dhis2.builder.PeriodMappingDataBuilder;
import org.openlmis.integration.dhis2.builder.ServerDataBuilder;
import org.openlmis.integration.dhis2.domain.periodmapping.PeriodMapping;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.periodmapping.PeriodMappingDto;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.web.period.PeriodMappingController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class PeriodMappingControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = PeriodMappingController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String AUDIT_LOG_URL = ID_URL + "/auditLog";
  private static final String SERVER_ID = "serverId";
  private static final String NAME = "name";

  private Server server = new ServerDataBuilder().build();
  private PeriodMapping periodMapping = new PeriodMappingDataBuilder().withServer(server).build();
  private PeriodMappingDto periodMappingDto = PeriodMappingDto.newInstance(periodMapping);

  private GlobalId globalId = new UnboundedValueObjectId(PeriodMapping.class.getSimpleName());
  private ValueChange change = new ValueChange(globalId, NAME, "name1", "name2");

  private CommitId commitId = new CommitId(1, 0);
  private CommitMetadata commitMetadata = new CommitMetadata(
          "admin", Maps.newHashMap(), LocalDateTime.now(), commitId);

  @Before
  public void setUp() {
    given(periodMappingRepository.saveAndFlush(any(PeriodMapping.class)))
            .willAnswer(new SaveAnswer<>());
    change.bindToCommit(commitMetadata);
    server.setPeriodMappingList(Collections.singletonList(periodMapping));
    mockUserHasManageIntegrationRight();
    mockUserHasManagePeriodsRight();
  }

  @Test
  public void shouldReturnPageOfPeriodMappings() {
    given(serverRepository.findById(periodMappingDto.getServerDto()
            .getId())).willReturn(Optional.of(server));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .queryParam("page", pageable.getPageNumber())
            .queryParam("size", pageable.getPageSize())
            .when()
            .get(RESOURCE_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("content", hasSize(1))
            .body("content[0].id", is(periodMapping.getId().toString()))
            .body("content[0].name", is(periodMapping.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAllPeriodMappingsEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured.given()
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .when()
            .get(RESOURCE_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreatePeriodMapping() {
    given(serverRepository.findById(periodMappingDto.getServerDto()
            .getId())).willReturn(Optional.of(server));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .body(periodMappingDto)
            .when()
            .post(RESOURCE_URL)
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .body(ID, is(notNullValue()))
            .body(NAME, is(periodMappingDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForCreatePeriodMappingEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .body(periodMappingDto)
            .when()
            .post(RESOURCE_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenPeriodMapping() {
    given(periodMappingRepository.findById(periodMappingDto.getId()))
            .willReturn(Optional.of(periodMapping));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .when()
            .get(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body(ID, is(periodMappingDto.getId().toString()))
            .body(NAME, is(periodMappingDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfPeriodMappingDoesNotExistForGivenEndpoint() {
    given(periodMappingRepository.findById(periodMappingDto.getId())).willReturn(Optional.empty());

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .when()
            .get(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERIOD_MAPPING_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetPeriodMappingEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured
            .given()
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .when()
            .get(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdatePeriodMapping() {
    given(periodMappingRepository.findById(periodMappingDto.getId()))
            .willReturn(Optional.of(periodMapping));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .body(periodMappingDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body(ID, is(periodMappingDto.getId().toString()))
            .body(NAME, is(periodMappingDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestMessageIfPeriodMappingCannotBeUpdated() {
    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, UUID.randomUUID().toString())
            .body(periodMappingDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERIOD_MAPPING_ID_MISMATCH));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForUpdatePeriodMappingEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .body(periodMappingDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeletePeriodMapping() {
    given(periodMappingRepository.existsById(periodMappingDto.getId())).willReturn(true);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .when()
            .delete(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfPeriodMappingDoesNotExistForDeleteEndpoint() {
    given(periodMappingRepository.existsById(periodMappingDto.getId())).willReturn(false);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .when()
            .delete(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERIOD_MAPPING_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeletePeriodMappingEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured
            .given()
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .when()
            .delete(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAuditLogs() {
    given(periodMappingRepository.existsById(periodMappingDto.getId())).willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .when()
            .get(AUDIT_LOG_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("", hasSize(1))
            .body("changeType", hasItem(change.getClass().getSimpleName()))
            .body("globalId.valueObject", hasItem(PeriodMapping.class.getSimpleName()))
            .body("commitMetadata.author", hasItem(commitMetadata.getAuthor()))
            .body("commitMetadata.properties", hasItem(hasSize(0)))
            .body("commitMetadata.commitDate", hasItem(commitMetadata.getCommitDate().toString()))
            .body("commitMetadata.id", hasItem(commitId.valueAsNumber().floatValue()))
            .body("property", hasItem(change.getPropertyName()))
            .body("left", hasItem(change.getLeft().toString()))
            .body("right", hasItem(change.getRight().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAuditLogsWithParameters() {
    given(periodMappingRepository.existsById(periodMappingDto.getId())).willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .queryParam("author", commitMetadata.getAuthor())
            .queryParam("changedPropertyName", change.getPropertyName())
            .when()
            .get(AUDIT_LOG_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("", hasSize(1))
            .body("changeType", hasItem(change.getClass().getSimpleName()))
            .body("globalId.valueObject", hasItem(PeriodMapping.class.getSimpleName()))
            .body("commitMetadata.author", hasItem(commitMetadata.getAuthor()))
            .body("commitMetadata.properties", hasItem(hasSize(0)))
            .body("commitMetadata.commitDate", hasItem(commitMetadata.getCommitDate().toString()))
            .body("commitMetadata.id", hasItem(commitId.valueAsNumber().floatValue()))
            .body("property", hasItem(change.getPropertyName()))
            .body("left", hasItem(change.getLeft().toString()))
            .body("right", hasItem(change.getRight().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfPeriodMappingDoesNotExistForAuditLogEndpoint() {
    given(periodMappingRepository.existsById(periodMappingDto.getId())).willReturn(false);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .when()
            .get(AUDIT_LOG_URL)
            .then()
            .statusCode(HttpStatus.SC_NOT_FOUND);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAuditLogEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured
            .given()
            .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
            .pathParam(ID, periodMappingDto.getId().toString())
            .when()
            .get(AUDIT_LOG_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetPeriodMappingRequestIfUserHasNoRight() {
    mockUserHasNoRight();
    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
        .pathParam(ID, periodMappingDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_NO_FOLLOWING_PERMISSION)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetPageOfPeriodMappingsIfUserHasNoRight() {
    mockUserHasNoRight();
    given(serverRepository.findById(periodMappingDto.getServerDto()
        .getId())).willReturn(Optional.of(server));

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
        .queryParam("page", pageable.getPageNumber())
        .queryParam("size", pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_NO_FOLLOWING_PERMISSION)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreatePeriodMappingIfUserHasNoRight() {
    mockUserHasNoRight();
    given(serverRepository.findById(periodMappingDto.getServerDto()
        .getId())).willReturn(Optional.of(server));

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
        .body(periodMappingDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_NO_FOLLOWING_PERMISSION)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectUpdatePeriodMappingIfUserHasNoRight() {
    mockUserHasNoRight();
    given(periodMappingRepository.findById(periodMappingDto.getId()))
        .willReturn(Optional.of(periodMapping));

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
        .pathParam(ID, periodMappingDto.getId().toString())
        .body(periodMappingDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_NO_FOLLOWING_PERMISSION)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeletePeriodMappingIfUserHasNoRight() {
    mockUserHasNoRight();
    given(periodMappingRepository.existsById(periodMappingDto.getId())).willReturn(true);

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
        .pathParam(ID, periodMappingDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_NO_FOLLOWING_PERMISSION)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectRetrieveAuditLogsIfUserHasNoRights() {
    mockUserHasNoRight();
    given(periodMappingRepository.existsById(periodMappingDto.getId())).willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, periodMappingDto.getServerDto().getId().toString())
        .pathParam(ID, periodMappingDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_NO_FOLLOWING_PERMISSION)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

}