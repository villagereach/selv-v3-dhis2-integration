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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dhis.DhisCategoryOptionCombo;
import org.openlmis.integration.dhis2.dto.dhis.DhisDataElement;
import org.openlmis.integration.dhis2.dto.dhis.DhisDataset;
import org.openlmis.integration.dhis2.dto.dhis.DhisElementCombo;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.dataset.DatasetRepository;
import org.openlmis.integration.dhis2.service.DhisDataService;
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
 * Controller used to expose concatenated DHIS2 Data Elements with Category Option Combos via HTTP.
 */
@Controller
@RequestMapping(DhisElementComboController.RESOURCE_PATH)
@Transactional
public class DhisElementComboController extends BaseController {

  public static final String RESOURCE_PATH = DatasetController.RESOURCE_PATH
      + "/{datasetId}/elementsAndCombos";

  @Autowired
  private DatasetRepository datasetRepository;

  @Autowired
  private DhisDataService dhisDataService;

  /**
   * Retrieves all dhis element and category option combos combinations for a given server.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<DhisElementCombo> getElementsAndCombos(
      @PathVariable("serverId") UUID serverId, @PathVariable("datasetId") UUID datasetId,
      Pageable pageable) {
    Dataset dataset = datasetRepository.findById(datasetId)
        .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_DATASET_NOT_FOUND));
    Server server = dataset.getServer();

    String dhisDatasetId = dataset.getDhisDatasetId();
    DhisDataset dhisDataset = dhisDataService.getDhisDataSetById(dhisDatasetId, server.getUrl(),
        server.getUsername(), server.getPassword());
    List<DhisDataElement> dhisDataElements = dhisDataset.getDataSetElements();

    List<DhisCategoryOptionCombo> categoryOptionCombos = dhisDataService
        .getDhisCategoryOptionCombos(server.getUrl(), server.getUsername(), server.getPassword());

    List<DhisElementCombo> dhisElementCombos = new ArrayList<>();
    for (DhisCategoryOptionCombo combo: categoryOptionCombos) {
      for (DhisDataElement element: dhisDataElements) {
        DhisElementCombo dhisElementCombo = new DhisElementCombo(
            element.getName() + " - " + combo.getDisplayName(),
            element.getName(),
            combo.getDisplayName()
        );
        dhisElementCombos.add(dhisElementCombo);
      }
    }

    return Pagination.getPage(dhisElementCombos, pageable);
  }

}