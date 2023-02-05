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

package org.openlmis.integration.dhis2.repository;

import java.util.UUID;
import org.openlmis.integration.dhis2.DataElementDataBuilder;
import org.openlmis.integration.dhis2.DatasetDataBuilder;
import org.openlmis.integration.dhis2.ServerDataBuilder;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.repository.dataset.DatasetRepository;
import org.openlmis.integration.dhis2.repository.element.DataElementRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class DataElementRepositoryIntegrationTest extends
        BaseCrudRepositoryIntegrationTest<DataElement> {

  @Autowired
  private DataElementRepository dataElementRepository;

  @Autowired
  private DatasetRepository datasetRepository;

  @Autowired
  private ServerRepository serverRepository;

  @Override
  public CrudRepository<DataElement, UUID> getRepository() {
    return dataElementRepository;
  }

  @Override
  public DataElement generateInstance() {
    Server server = new ServerDataBuilder().buildAsNew();
    serverRepository.save(server);

    Dataset dataset = new DatasetDataBuilder().withServer(server).buildAsNew();
    datasetRepository.save(dataset);
    return new DataElementDataBuilder().withDataset(dataset).buildAsNew();
  }

}
