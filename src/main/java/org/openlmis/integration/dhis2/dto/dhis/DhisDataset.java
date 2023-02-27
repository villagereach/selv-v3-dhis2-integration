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

package org.openlmis.integration.dhis2.dto.dhis;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Objects of this class represent data retrieved from the DHIS2 API.
 * Dataset is the collection of {@link DhisDataElement}. A dataset doesn't store values,
 * it's just a grouped collection of data, so deleting a given dataset doesn't delete
 * the data values.
 * @see <a href="https://docs.dhis2.org/">DHIS2 Documentation</a>
 */
@ToString
public class DhisDataset {

  @Getter
  @Setter
  private String id;

  @Getter
  @Setter
  private String displayName;

  @Getter
  @Setter
  private String periodType;

  @Setter
  private List<Map<Object, DhisDataElement>> dataSetElements;

  @Getter
  @Setter
  private List<OrganisationUnit> organisationUnits;

  /**
   * Returns a list of data elements for this dataset.
   *
   * @return list of data elements.
   */
  public List<DhisDataElement> getDataSetElements() {
    return dataSetElements.stream()
            .flatMap(e -> e.values().stream())
            .collect(Collectors.toList());
  }

}