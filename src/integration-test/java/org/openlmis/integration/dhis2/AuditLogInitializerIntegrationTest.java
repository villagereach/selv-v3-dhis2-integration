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

package org.openlmis.integration.dhis2;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.repository.jql.QueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles({"test", "init-audit-log"})
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuditLogInitializerIntegrationTest {

  private static final String[] SERVER_FIELDS = {
      "id", "name", "url", "username", "password"
  };

  private static final String[] DATASET_FIELDS = {
      "id", "name", "dhisDatasetId", "cronExpression", "timeOffset", "serverId"
  };

  private static final String[] DATAELEMENT_FIELDS = {
      "id", "name", "source", "indicator", "orderable", "element", "datasetId"
  };

  private static final String INSERT_SERVER_SQL = String.format(
      "INSERT INTO dhis2.server (%s) VALUES (%s) ",
      StringUtils.join(SERVER_FIELDS, ", "),
      StringUtils.repeat("?", ", ", SERVER_FIELDS.length)
  );

  private static final String INSERT_DATASET_SQL = String.format(
      "INSERT INTO dhis2.dataset (%s) VALUES (%s) ",
      StringUtils.join(DATASET_FIELDS, ", "),
      StringUtils.repeat("?", ", ", DATASET_FIELDS.length)
  );

  private static final String INSERT_DATAELEMENT_SQL = String.format(
          "INSERT INTO dhis2.data_element (%s) VALUES (%s) ",
          StringUtils.join(DATAELEMENT_FIELDS, ", "),
          StringUtils.repeat("?", ", ", DATAELEMENT_FIELDS.length)
  );

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private Javers javers;

  @PersistenceContext
  private EntityManager entityManager;

  @Test
  public void shouldCreateSnapshotForServer() {
    UUID serverId = UUID.randomUUID();
    addServer(serverId);

    executeTest(serverId, Server.class);
  }

  @Test
  public void shouldCreateSnapshotForDataset() {
    UUID serverId = UUID.randomUUID();
    UUID datasetId = UUID.randomUUID();

    addServer(serverId);
    addDataset(datasetId, serverId);

    executeTest(datasetId, Dataset.class);
  }

  @Test
  public void shouldCreateSnapshotForDataElement() {
    UUID serverId = UUID.randomUUID();
    UUID datasetId = UUID.randomUUID();
    UUID dataElementId = UUID.randomUUID();

    addServer(serverId);
    addDataset(datasetId, serverId);
    addDataElement(dataElementId, datasetId);

    executeTest(dataElementId, DataElement.class);
  }

  private void executeTest(Object id, Class clazz) {
    //when
    QueryBuilder jqlQuery = QueryBuilder.byInstanceId(id, clazz);
    List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

    assertThat(snapshots, hasSize(0));

    AuditLogInitializer auditLogInitializer = new AuditLogInitializer(applicationContext, javers);
    auditLogInitializer.run();

    snapshots = javers.findSnapshots(jqlQuery.build());

    // then
    assertThat(snapshots, hasSize(1));

    CdoSnapshot snapshot = snapshots.get(0);
    GlobalId globalId = snapshot.getGlobalId();

    assertThat(globalId, is(notNullValue()));
    assertThat(globalId, instanceOf(InstanceId.class));

    InstanceId instanceId = (InstanceId) globalId;
    assertThat(instanceId.getCdoId(), is(id));
    assertThat(instanceId.getTypeName(), is(clazz.getSimpleName()));
  }

  private void addServer(UUID id) {
    entityManager.flush();
    entityManager
            .createNativeQuery(INSERT_SERVER_SQL)
            .setParameter(1, id)
            .setParameter(2, "test-name")
            .setParameter(3, "http://test.configuration")
            .setParameter(4, "test-username")
            .setParameter(5, "$2a$12$/MRrjNIDYgba/9K6i.zNAOSMJFkWWwJHVYXGp/s3OfSbWL1fsiMWG")
            .executeUpdate();
  }

  private void addDataset(UUID id, UUID serverId) {
    entityManager.flush();
    entityManager
            .createNativeQuery(INSERT_DATASET_SQL)
            .setParameter(1, id)
            .setParameter(2, "test-name")
            .setParameter(3, "idXfoem")
            .setParameter(4, "DAILY")
            .setParameter(5, "90")
            .setParameter(6, serverId)
            .executeUpdate();
  }

  private void addDataElement(UUID id, UUID datasetId) {
    entityManager.flush();
    entityManager
            .createNativeQuery(INSERT_DATAELEMENT_SQL)
            .setParameter(1, id)
            .setParameter(2, "test-name")
            .setParameter(3, "test-source")
            .setParameter(4, "test-indicator")
            .setParameter(5, "test-orderable")
            .setParameter(6, "test-element")
            .setParameter(7, datasetId)
            .executeUpdate();
  }

}
