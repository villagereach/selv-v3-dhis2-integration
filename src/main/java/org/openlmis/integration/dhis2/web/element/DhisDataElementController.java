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

package org.openlmis.integration.dhis2.web.element;

import java.util.UUID;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dhis.DhisDataElement;
import org.openlmis.integration.dhis2.dto.dhis.DhisDataset;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.dataset.DatasetRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.openlmis.integration.dhis2.service.communication.DhisDataService;
import org.openlmis.integration.dhis2.service.role.PermissionService;
import org.openlmis.integration.dhis2.util.Pagination;
import org.openlmis.integration.dhis2.web.BaseController;
import org.openlmis.integration.dhis2.web.dataset.DatasetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller used to expose DHIS2 Data Elements via HTTP.
 */
@Controller
@RequestMapping(DhisDataElementController.RESOURCE_PATH)
@Transactional
public class DhisDataElementController extends BaseController {

  public static final String RESOURCE_PATH = DatasetController.RESOURCE_PATH
          + "/{datasetId}/dhisElements";

  @Autowired
  private ServerRepository serverRepository;

  @Autowired
  private DatasetRepository datasetRepository;

  @Autowired
  private DhisDataService dhisDataService;

  @Autowired
  private PermissionService permissionService;

  /**
   * Retrieves the all dhis data elements for a given dataset.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<DhisDataElement> getAllDataElementsForDataset(
          @PathVariable("serverId") UUID serverId,
          @PathVariable("datasetId") UUID datasetId,
          Pageable pageable) {
    permissionService.canManageDhisIntegration();

    Server server = serverRepository.findById(serverId)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND));
    Dataset dataset = datasetRepository.findById(datasetId)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_DATASET_NOT_FOUND));

    String dhisDatasetId = dataset.getDhisDatasetId();
    DhisDataset dhisDataset = dhisDataService.getDhisDataSetById(dhisDatasetId, server.getUrl(),
            server.getUsername(), server.getPassword());

    return Pagination.getPage(dhisDataset.getDataSetElements(), pageable);
  }

}
