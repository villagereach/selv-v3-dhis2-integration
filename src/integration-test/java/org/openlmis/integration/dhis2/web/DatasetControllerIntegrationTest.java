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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Matchers.any;

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
import org.openlmis.integration.dhis2.DatasetDataBuilder;
import org.openlmis.integration.dhis2.ServerDataBuilder;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dataset.DatasetDto;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.web.dataset.DatasetController;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class DatasetControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = DatasetController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String AUDIT_LOG_URL = ID_URL + "/auditLog";

  private static final String NAME = "name";
  private Server server = new ServerDataBuilder().build();

  private Dataset dataset = new DatasetDataBuilder().withServer(server).build();
  private DatasetDto datasetDto = DatasetDto.newInstance(dataset);

  private GlobalId globalId = new UnboundedValueObjectId(Dataset.class.getSimpleName());
  private ValueChange change = new ValueChange(globalId, NAME, "name1", "name2");

  private CommitId commitId = new CommitId(1, 0);
  private CommitMetadata commitMetadata = new CommitMetadata(
          "admin", Maps.newHashMap(), LocalDateTime.now(), commitId);

  @Before
  public void setUp() {
    given(datasetRepository.saveAndFlush(any(Dataset.class))).willAnswer(new SaveAnswer<>());
    change.bindToCommit(commitMetadata);
  }

  @Test
  public void shouldReturnPageOfDatasets() {
    given(datasetRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.singletonList(dataset)));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", pageable.getPageNumber())
        .queryParam("size", pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(1))
        .body("content[0].id", is(dataset.getId().toString()))
        .body("content[0].name", is(dataset.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAllDatasetsEndpointIfUserIsNotAuthorized() {
    restAssured.given()
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateDataset() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(datasetDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body(ID, is(notNullValue()))
        .body(NAME, is(datasetDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForCreateDatasetEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(datasetDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenDataset() {
    given(datasetRepository.findById(datasetDto.getId())).willReturn(Optional.of(dataset));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, datasetDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(datasetDto.getId().toString()))
        .body(NAME, is(datasetDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfDatasetDoesNotExistForGivenDatasetEndpoint() {
    given(datasetRepository.findById(datasetDto.getId())).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, datasetDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_DATASET_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetDatasetEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .pathParam(ID, datasetDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateDataset() {
    given(datasetRepository.findById(datasetDto.getId())).willReturn(Optional.of(dataset));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, datasetDto.getId().toString())
        .body(datasetDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(datasetDto.getId().toString()))
        .body(NAME, is(datasetDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestMessageIfDatasetCannotBeUpdated() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, UUID.randomUUID().toString())
        .body(datasetDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_DATASET_ID_MISMATCH));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForUpdateDatasetEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, datasetDto.getId().toString())
        .body(datasetDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteDataset() {
    given(datasetRepository.existsById(datasetDto.getId())).willReturn(true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, datasetDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfDatasetDoesNotExistForDeleteDatasetEndpoint() {
    given(datasetRepository.existsById(datasetDto.getId())).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, datasetDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_DATASET_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeleteDatasetEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .pathParam(ID, datasetDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAuditLogs() {
    given(datasetRepository.existsById(datasetDto.getId())).willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, datasetDto.getId().toString())
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
    given(datasetRepository.existsById(datasetDto.getId())).willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, datasetDto.getId().toString())
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
  public void shouldReturnNotFoundMessageIfDatasetDoesNotExistForAuditLogEndpoint() {
    given(datasetRepository.existsById(datasetDto.getId())).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, datasetDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAuditLogEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .pathParam(ID, datasetDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
