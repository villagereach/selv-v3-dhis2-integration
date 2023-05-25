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

package org.openlmis.integration.dhis2.web.period;

import static org.openlmis.integration.dhis2.web.period.PeriodMappingController.RESOURCE_PATH;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.integration.dhis2.domain.periodmapping.PeriodMapping;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.periodmapping.PeriodMappingDto;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.exception.ValidationMessageException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.periodmapping.PeriodMappingRepository;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.openlmis.integration.dhis2.service.role.PermissionService;
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
 * Controller used to expose PeriodMapping via HTTP.
 */
@Controller
@RequestMapping(RESOURCE_PATH)
@Transactional
public class PeriodMappingController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PeriodMappingController.class);

  public static final String RESOURCE_PATH = ServerController.RESOURCE_PATH
          + "/{serverId}/periodMappings";

  @Autowired
  private PeriodMappingRepository periodMappingRepository;

  @Autowired
  private ServerRepository serverRepository;

  @Autowired
  private PermissionService permissionService;

  /**
   * Retrieves the specified period mapping.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public PeriodMappingDto getPeriodMapping(@PathVariable("id") UUID id) {
    permissionService.canManageDhisIntegration();
    PeriodMapping periodMapping = periodMappingRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_PERIOD_MAPPING_NOT_FOUND));

    return PeriodMappingDto.newInstance(periodMapping);
  }

  /**
   * Retrieves all period mappings. Note that an empty collection rather than a 404 should be
   * returned if no period mappings exist.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<PeriodMappingDto> getAllPeriodMappings(@PathVariable("serverId") UUID serverId,
                                                     Pageable pageable) {
    permissionService.canManageDhisIntegration();
    Server server = serverRepository.findById(serverId)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND));

    List<PeriodMapping> periodMappings = server.getPeriodMappingList();
    List<PeriodMappingDto> periodMappingDtos = periodMappings
            .stream()
            .map(PeriodMappingDto::newInstance)
            .collect(Collectors.toList());

    return Pagination.getPage(periodMappingDtos, pageable);
  }

  /**
   * Allows the creation of a new period mapping. If the id is specified, it will be ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public PeriodMappingDto createPeriodMapping(@PathVariable("serverId") UUID serverId,
                                              @RequestBody PeriodMappingDto periodMappingDto) {
    permissionService.canManageDhisIntegration();
    LOGGER.debug("Creating new period mapping");
    PeriodMapping newPeriodMapping = PeriodMapping.newInstance(periodMappingDto);

    Server server = serverRepository.findById(serverId)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND));
    newPeriodMapping.setServer(server);

    newPeriodMapping.setId(null);
    newPeriodMapping = periodMappingRepository.saveAndFlush(newPeriodMapping);

    return PeriodMappingDto.newInstance(newPeriodMapping);
  }

  /**
   * Updates the specified period mapping.
   */
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public PeriodMappingDto updatePeriodMapping(@PathVariable("id") UUID id,
                                              @RequestBody PeriodMappingDto periodMappingDto) {
    permissionService.canManageDhisIntegration();
    if (null != periodMappingDto.getId() && !Objects.equals(periodMappingDto.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_PERIOD_MAPPING_ID_MISMATCH);
    }

    LOGGER.debug("Updating period mapping");
    PeriodMapping periodMappingToSave = periodMappingRepository.findById(id).map(periodMapping -> {
      periodMapping.updateFrom(periodMappingDto);
      return periodMapping;
    }).orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_PERIOD_MAPPING_NOT_FOUND));

    periodMappingRepository.saveAndFlush(periodMappingToSave);
    return PeriodMappingDto.newInstance(periodMappingToSave);
  }

  /**
   * Deletes the specified period mapping.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePeriodMapping(@PathVariable("id") UUID id) {
    permissionService.canManageDhisIntegration();
    if (!periodMappingRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_PERIOD_MAPPING_NOT_FOUND);
    }

    periodMappingRepository.deleteById(id);
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
  public ResponseEntity<String> getPeriodMappingAuditLog(@PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName, Pageable page) {
    permissionService.canManageDhisIntegration();

    // Return a 404 if the specified instance can't be found
    if (!periodMappingRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_PERIOD_MAPPING_NOT_FOUND);
    }

    return getAuditLogResponse(PeriodMapping.class, id, author, changedPropertyName, page);
  }

}
