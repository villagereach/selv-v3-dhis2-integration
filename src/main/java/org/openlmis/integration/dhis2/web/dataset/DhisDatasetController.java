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

package org.openlmis.integration.dhis2.web.dataset;

import java.util.UUID;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dhis.DhisDataset;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.openlmis.integration.dhis2.service.DhisDataService;
import org.openlmis.integration.dhis2.util.Pagination;
import org.openlmis.integration.dhis2.web.BaseController;
import org.openlmis.integration.dhis2.web.server.ServerController;
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
 * Controller used to expose DHIS2 datasets via HTTP.
 */
@Controller
@RequestMapping(DhisDatasetController.RESOURCE_PATH)
@Transactional
public class DhisDatasetController extends BaseController {

  public static final String RESOURCE_PATH = ServerController.RESOURCE_PATH
          + "/{serverId}/dhisDatasets";

  @Autowired
  private ServerRepository serverRepository;

  @Autowired
  private DhisDataService dhisDataService;

  /**
   * Retrieves the all dhis datasets for a given server.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<DhisDataset> getAllDatasets(@PathVariable("serverId") UUID serverId,
                                          Pageable pageable) {
    Server server = serverRepository.findById(serverId)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND));

    return Pagination.getPage(dhisDataService.getDhisDatasets(server.getUrl(),
            server.getUsername(), server.getPassword()), pageable);
  }

}
