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

package org.openlmis.integration.dhis2.web.server;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.integration.dhis2.domain.server.Server;
import org.openlmis.integration.dhis2.dto.server.ServerDto;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.exception.ValidationMessageException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.server.ServerRepository;
import org.openlmis.integration.dhis2.service.PermissionService;
import org.openlmis.integration.dhis2.util.Pagination;
import org.openlmis.integration.dhis2.web.BaseController;
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
@RequestMapping(ServerController.RESOURCE_PATH)
@Transactional
public class ServerController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);
  public static final String RESOURCE_PATH = API_PATH + "/serverConfiguration";

  @Autowired
  private ServerRepository serverRepository;

  @Autowired
  private PermissionService permissionService;

  /**
   * Retrieves the specified server.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ServerDto getServer(@PathVariable("id") UUID id) {
    permissionService.canManageDhisIntegration();
    Server server = serverRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND));

    return ServerDto.newInstance(server);
  }

  /**
   * Retrieves all servers. Note that an empty collection rather than a 404 should be
   * returned if no servers exist.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<ServerDto> getAllServers(Pageable pageable) {
    permissionService.canManageDhisIntegration();
    Page<Server> page = serverRepository.findAll(pageable);
    List<ServerDto> content = page
            .getContent()
            .stream()
            .map(ServerDto::newInstance)
            .collect(Collectors.toList());
    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Allows the creation of a new server. If the id is specified, it will be ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ServerDto createServer(@RequestBody ServerDto serverDto) {
    permissionService.canManageDhisIntegration();
    LOGGER.debug("Creating new server");

    Server newServer = Server.newInstance(serverDto);
    newServer.setId(null);
    newServer = serverRepository.saveAndFlush(newServer);

    return ServerDto.newInstance(newServer);
  }

  /**
   * Updates the specified server.
   */
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ServerDto updateServer(@PathVariable("id") UUID id, @RequestBody ServerDto serverDto) {
    permissionService.canManageDhisIntegration();
    if (null != serverDto.getId() && !Objects.equals(serverDto.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_SERVER_ID_MISMATCH);
    }

    LOGGER.debug("Updating server");
    Server serverToSave = serverRepository.findById(id).map(server -> {
      server.updateFrom(serverDto);
      return server;
    }).orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND));

    serverRepository.saveAndFlush(serverToSave);
    return ServerDto.newInstance(serverToSave);
  }

  /**
   * Deletes the specified server.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteServer(@PathVariable("id") UUID id) {
    permissionService.canManageDhisIntegration();
    if (!serverRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND);
    }

    serverRepository.deleteById(id);
  }

  /**
   * Retrieves audit information related to the specified server.
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
  public ResponseEntity<String> getServerAuditLog(@PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName, Pageable page) {
    permissionService.canManageDhisIntegration();

    // Return a 404 if the specified instance can't be found
    if (!serverRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_SERVER_NOT_FOUND);
    }

    return getAuditLogResponse(Server.class, id, author, changedPropertyName, page);
  }

}
