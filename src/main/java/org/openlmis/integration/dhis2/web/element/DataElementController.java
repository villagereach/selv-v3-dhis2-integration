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

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.integration.dhis2.domain.dataset.Dataset;
import org.openlmis.integration.dhis2.domain.element.DataElement;
import org.openlmis.integration.dhis2.dto.element.DataElementDto;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.exception.ValidationMessageException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.dataset.DatasetRepository;
import org.openlmis.integration.dhis2.repository.element.DataElementRepository;
import org.openlmis.integration.dhis2.util.Pagination;
import org.openlmis.integration.dhis2.web.BaseController;
import org.openlmis.integration.dhis2.web.dataset.DatasetController;
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
 * Controller used to expose Data Elements via HTTP.
 */
@Controller
@RequestMapping(DataElementController.RESOURCE_PATH)
@Transactional
public class DataElementController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataElementController.class);

  public static final String RESOURCE_PATH = DatasetController.RESOURCE_PATH
          + "/{datasetId}/elements";

  @Autowired
  private DataElementRepository dataElementRepository;

  @Autowired
  private DatasetRepository datasetRepository;

  /**
   * Retrieves the specified data element.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public DataElementDto getDataElement(@PathVariable("id") UUID id) {
    DataElement dataElement = dataElementRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_DATAELEMENT_NOT_FOUND));

    return DataElementDto.newInstance(dataElement);
  }

  /**
   * Retrieves all data elements. Note that an empty collection rather than a 404 should be
   * returned if no data elements exist.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<DataElementDto> getAllDataElements(@PathVariable("datasetId") UUID datasetId,
                                         Pageable pageable) {
    Dataset dataset = datasetRepository.findById(datasetId)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_DATAELEMENT_NOT_FOUND));

    List<DataElement> dataElements = dataset.getDataElementList();
    List<DataElementDto> dataElementDtos = dataElements
            .stream()
            .map(DataElementDto::newInstance)
            .collect(Collectors.toList());

    return Pagination.getPage(dataElementDtos, pageable);
  }

  /**
   * Allows the creation of a new data element. If the id is specified, it will be ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public DataElementDto createDataElement(@PathVariable("datasetId") UUID datasetId,
                                  @RequestBody DataElementDto dataElementDto) {
    LOGGER.debug("Creating new data element");
    DataElement newDataElement = DataElement.newInstance(dataElementDto);

    Dataset dataset = datasetRepository.findById(datasetId)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_DATAELEMENT_NOT_FOUND));
    newDataElement.setDataset(dataset);

    newDataElement.setId(null);
    newDataElement = dataElementRepository.saveAndFlush(newDataElement);

    return DataElementDto.newInstance(newDataElement);
  }

  /**
   * Updates the specified data element.
   */
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public DataElementDto updateDataElement(@PathVariable("id") UUID id,
                                          @RequestBody DataElementDto dataElementDto) {
    if (null != dataElementDto.getId() && !Objects.equals(dataElementDto.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_DATAELEMENT_ID_MISMATCH);
    }

    LOGGER.debug("Updating data element");
    DataElement dataElementToSave = dataElementRepository.findById(id).map(dataElement -> {
      dataElement.updateFrom(dataElementDto);
      return dataElement;
    }).orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_DATAELEMENT_NOT_FOUND));

    dataElementRepository.saveAndFlush(dataElementToSave);
    return DataElementDto.newInstance(dataElementToSave);
  }

  /**
   * Deletes the specified data element.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDataElement(@PathVariable("id") UUID id) {
    if (!dataElementRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_DATAELEMENT_NOT_FOUND);
    }

    dataElementRepository.deleteById(id);
  }

  /**
   * Retrieves audit information related to the specified data element.
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
  public ResponseEntity<String> getDataElementAuditLog(@PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName, Pageable page) {

    // Return a 404 if the specified instance can't be found
    if (!dataElementRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_DATAELEMENT_NOT_FOUND);
    }

    return getAuditLogResponse(DataElement.class, id, author, changedPropertyName, page);
  }

}
