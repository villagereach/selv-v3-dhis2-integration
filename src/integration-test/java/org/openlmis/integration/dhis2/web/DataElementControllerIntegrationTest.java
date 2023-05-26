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
import org.openlmis.integration.dhis2.builder.DataElementDataBuilder;
import org.openlmis.integration.dhis2.builder.DatasetDataBuilder;
import org.openlmis.integration.dhis2.builder.ServerDataBuilder;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dataset.DatasetDto;
import org.openlmis.integration.dhis2.dto.element.DataElementDto;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.service.schedule.ScheduleService;
import org.openlmis.integration.dhis2.web.element.DataElementController;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class DataElementControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = DataElementController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String AUDIT_LOG_URL = ID_URL + "/auditLog";
  private static final String SERVER_ID = "serverId";
  private static final String DATASET_ID = "datasetId";
  private static final String NAME = "name";

  private Server server = new ServerDataBuilder().build();
  private Dataset dataset = new DatasetDataBuilder().withServer(server).build();
  private DataElement dataElement = new DataElementDataBuilder().withDataset(dataset).build();

  private DatasetDto datasetDto = DatasetDto.newInstance(dataset);
  private DataElementDto dataElementDto = DataElementDto.newInstance(dataElement);

  private GlobalId globalId = new UnboundedValueObjectId(Dataset.class.getSimpleName());
  private ValueChange change = new ValueChange(globalId, NAME, "name1", "name2");

  private CommitId commitId = new CommitId(1, 0);
  private CommitMetadata commitMetadata = new CommitMetadata(
          "admin", Maps.newHashMap(), LocalDateTime.now(), commitId);

  @MockBean
  ScheduleService scheduleService;

  @Before
  public void setUp() {
    given(datasetRepository.saveAndFlush(any(Dataset.class)))
            .willAnswer(new SaveAnswer<>());
    given(dataElementRepository.saveAndFlush(any(DataElement.class)))
            .willAnswer(new SaveAnswer<>());
    change.bindToCommit(commitMetadata);
    server.setDatasetList(Collections.singletonList(dataset));
    dataset.setDataElementList(Collections.singletonList(dataElement));
    mockUserHasManageIntegrationRight();
  }

  @Test
  public void shouldReturnPageOfDataElements() {
    given(datasetRepository.findById(dataElementDto.getDatasetDto()
            .getId())).willReturn(Optional.of(dataset));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .queryParam("page", pageable.getPageNumber())
        .queryParam("size", pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(1))
        .body("content[0].id", is(dataElement.getId().toString()))
        .body("content[0].name", is(dataElement.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAllDataElementsEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured.given()
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateDataElement() {
    given(datasetRepository.findById(dataElementDto.getDatasetDto()
            .getId())).willReturn(Optional.of(dataset));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .body(dataElementDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body(ID, is(notNullValue()))
        .body(NAME, is(dataElementDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForCreateDataElementEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .body(dataElementDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenDataElement() {
    given(dataElementRepository.findById(dataElementDto.getId()))
            .willReturn(Optional.of(dataElement));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(dataElementDto.getId().toString()))
        .body(NAME, is(dataElementDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfDataElementDoesNotExistForGivenDataElementEndpoint() {
    given(dataElementRepository.findById(dataElementDto.getId())).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_DATAELEMENT_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetDataElementEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured
        .given()
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateDataElement() {
    given(dataElementRepository.findById(dataElementDto.getId()))
            .willReturn(Optional.of(dataElement));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .body(dataElementDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(dataElementDto.getId().toString()))
        .body(NAME, is(dataElementDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestMessageIfDataElementCannotBeUpdated() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, UUID.randomUUID().toString())
        .body(dataElementDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_DATAELEMENT_ID_MISMATCH));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForUpdateDataElementEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .body(dataElementDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteDataElement() {
    given(dataElementRepository.existsById(dataElementDto.getId())).willReturn(true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfDataElementDoesNotExistForDeleteDataElementEndpoint() {
    given(dataElementRepository.existsById(dataElementDto.getId())).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_DATAELEMENT_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeleteDataElementEndpointIfUserIsNotAuthorized() {
    mockUserHasNoRight();
    restAssured
        .given()
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAuditLogs() {
    given(dataElementRepository.existsById(dataElementDto.getId())).willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("", hasSize(1))
        .body("changeType", hasItem(change.getClass().getSimpleName()))
        .body("globalId.valueObject", hasItem(Dataset.class.getSimpleName()))
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
    given(dataElementRepository.existsById(dataElementDto.getId())).willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .queryParam("author", commitMetadata.getAuthor())
        .queryParam("changedPropertyName", change.getPropertyName())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("", hasSize(1))
        .body("changeType", hasItem(change.getClass().getSimpleName()))
        .body("globalId.valueObject", hasItem(Dataset.class.getSimpleName()))
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
  public void shouldReturnNotFoundMessageIfDataElementDoesNotExistForAuditLogEndpoint() {
    given(dataElementRepository.existsById(dataElementDto.getId())).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
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
        .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
        .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
        .pathParam(ID, dataElementDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetDataElementRequestIfUserHasNoRight() {
    mockUserHasNoRight();
    given(dataElementRepository.findById(dataElementDto.getId()))
            .willReturn(Optional.of(dataElement));

    String response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
            .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
            .pathParam(ID, dataElementDto.getId().toString())
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
  public void shouldRejectGetPageOfDataElementsIfUserHasNoRight() {
    mockUserHasNoRight();
    given(datasetRepository.findById(dataElementDto.getDatasetDto()
            .getId())).willReturn(Optional.of(dataset));

    String response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
            .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
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
  public void shouldRejectCreateDataElementIfUserHasNoRight() {
    mockUserHasNoRight();
    given(datasetRepository.findById(dataElementDto.getDatasetDto()
            .getId())).willReturn(Optional.of(dataset));

    String response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
            .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
            .body(dataElementDto)
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
  public void shouldRejectUpdateDataElementIfUserHasNoRight() {
    mockUserHasNoRight();
    given(dataElementRepository.findById(dataElementDto.getId()))
            .willReturn(Optional.of(dataElement));

    String response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
            .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
            .pathParam(ID, dataElementDto.getId().toString())
            .body(dataElementDto)
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
  public void shouldRejectDeleteDataElementIfUserHasNoRight() {
    mockUserHasNoRight();
    given(dataElementRepository.existsById(dataElementDto.getId())).willReturn(true);

    String response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
            .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
            .pathParam(ID, dataElementDto.getId().toString())
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
    given(dataElementRepository.existsById(dataElementDto.getId())).willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    String response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(SERVER_ID, datasetDto.getServerDto().getId().toString())
            .pathParam(DATASET_ID, dataElementDto.getDatasetDto().getId().toString())
            .pathParam(ID, dataElementDto.getId().toString())
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
