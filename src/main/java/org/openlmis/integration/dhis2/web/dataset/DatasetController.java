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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.dataset.DatasetDto;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.exception.ValidationMessageException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.dataset.DatasetRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.openlmis.integration.dhis2.util.Pagination;
import org.openlmis.integration.dhis2.web.BaseController;
import org.openlmis.integration.dhis2.web.server.ServerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller used to expose Servers via HTTP.
 */
@Controller
@RequestMapping(DatasetController.RESOURCE_PATH)
@Transactional
public class DatasetController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

  public static final String RESOURCE_PATH = ServerController.RESOURCE_PATH + "/datasets";

  @Autowired
  private DatasetRepository datasetRepository;

  @Autowired
  private ServerRepository serverRepository;

  /**
   * Retrieves the specified dataset.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public DatasetDto getDataset(@PathVariable("id") UUID id) {
    Dataset dataset = datasetRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_DATASET_NOT_FOUND));

    return DatasetDto.newInstance(dataset);
  }

  /**
   * Retrieves all datasets. Note that an empty collection rather than a 404 should be
   * returned if no datasets exist.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<DatasetDto> getAllDatasets(Pageable pageable) {
    Page<Dataset> page = datasetRepository.findAll(pageable);
    List<DatasetDto> content = page
            .getContent()
            .stream()
            .map(DatasetDto::newInstance)
            .collect(Collectors.toList());
    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Allows the creation of a new dataset. If the id is specified, it will be ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public DatasetDto createDataset(@RequestBody DatasetDto datasetDto) {
    LOGGER.debug("Creating new dataset");
    Dataset newDataset = Dataset.newInstance(datasetDto);
    newDataset.setId(null);

    Server server = serverRepository.findById(datasetDto.getServerDto().getId())
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND));

    newDataset.setServer(server);
    newDataset = datasetRepository.saveAndFlush(newDataset);

    return DatasetDto.newInstance(newDataset);
  }

  /**
   * Updates the specified dataset.
   */
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public DatasetDto updateDataset(@PathVariable("id") UUID id, @RequestBody DatasetDto datasetDto) {
    if (null != datasetDto.getId() && !Objects.equals(datasetDto.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_DATASET_ID_MISMATCH);
    }

    LOGGER.debug("Updating dataset");
    Dataset dataset;
    Optional<Dataset> datasetOptional = datasetRepository.findById(id);
    if (datasetOptional.isPresent()) {
      dataset = datasetOptional.get();
      dataset.updateFrom(datasetDto);
    } else {
      dataset = Dataset.newInstance(datasetDto);
      dataset.setId(id);
    }

    datasetRepository.saveAndFlush(dataset);
    return DatasetDto.newInstance(dataset);
  }

  /**
   * Deletes the specified dataset.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDataset(@PathVariable("id") UUID id) {
    if (!datasetRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_DATASET_NOT_FOUND);
    }

    datasetRepository.deleteById(id);
  }

  /**
   * Retrieves audit information related to the specified dataset.
   *
   * @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @GetMapping(value = "/{id}/auditLog")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getDatasetAuditLog(@PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName, Pageable page) {

    // Return a 404 if the specified instance can't be found
    if (!datasetRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_DATASET_NOT_FOUND);
    }

    return getAuditLogResponse(Dataset.class, id, author, changedPropertyName, page);
  }

}
