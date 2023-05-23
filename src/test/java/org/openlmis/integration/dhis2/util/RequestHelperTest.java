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

package org.openlmis.integration.dhis2.util;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class RequestHelperTest {

  private static final String SERVER_URL = "https://play.dhis2.org/2.39.0.1";

  @Test
  public void shouldCreateUriWithoutParameters() {
    URI uri = RequestHelper.createUri(SERVER_URL, RequestParameters.init());
    assertThat(uri.getQuery(), is(nullValue()));
  }

  @Test
  public void shouldCreateUriWithParameters() throws Exception {
    URI uri = RequestHelper.createUri(SERVER_URL, RequestParameters.init().set("a", "b"));
    assertThat(uri.getQuery(), is("a=b"));
  }

  @Test
  public void shouldCreateUriWithEncodedParameters() throws Exception {
    URI uri = RequestHelper.createUri(SERVER_URL, RequestParameters.init().set("a", "b c"));
    assertThat(uri.getQuery(), is("a=b c"));
    assertThat(uri.getRawQuery(), is("a=b%20c"));
  }

  @Test
  public void shouldCreateEntity() {
    String token = "r4nd0m70k3n";
    String tokenPrefix = "Prefix";

    HttpEntity entity = RequestHelper.createEntity(token, tokenPrefix);

    assertThat(entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
            is(singletonList("Prefix r4nd0m70k3n")));
  }

}