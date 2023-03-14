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

import static java.lang.String.valueOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encodeQueryParam;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Creates a {@link URI} from the given string representation and with the given parameters.
 */
public final class RequestHelper {

  private RequestHelper() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a {@link URI} from the given string representation and with the given parameters.
   */
  public static URI createUri(String url, RequestParameters parameters) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance().uri(URI.create(url));

    if (parameters != null) {
      parameters.forEach(pm -> {
        builder.queryParam(pm.getKey(), encodeQueryParam(valueOf(pm.getValue()), UTF_8.name()));
      });
    }

    return builder.build(true).toUri();
  }

  /**
   * Creates an {@link HttpEntity} and adds an authorization header with the provided token
   * and prefix.
   *
   * @param token       The token to put into the authorization header.
   * @param tokenPrefix The string that will be placed in the auth header value before the token.
   * @return the {@link HttpEntity} to use
   */
  public static <E> HttpEntity<E> createEntity(String token, String tokenPrefix) {
    return new HttpEntity<>(createHeadersWithAuth(token, tokenPrefix));
  }

  /**
   * Creates an {@link HttpEntity} with the given payload as a body, token and tokenPrefix.
   */
  public static <E> HttpEntity<E> createEntity(E payload, String token, String tokenPrefix) {
    if (payload == null) {
      return createEntity(token, tokenPrefix);
    } else {
      return createEntity(payload, createHeadersWithAuth(token, tokenPrefix));
    }
  }

  /**
   * Creates an {@link HttpEntity} with the given payload as a body and headers.
   */
  public static <E> HttpEntity<E> createEntity(E payload, HttpHeaders headers) {
    return new HttpEntity<>(payload, headers);
  }

  /**
   * Creates an {@link HttpEntity} with the given payload as a body and headers.
   */
  public static <E> HttpEntity<E> createEntity(E payload, RequestHeaders headers) {
    return new HttpEntity<>(payload, headers.toHeaders());
  }

  /**
   * Creates an {@link HttpEntity} with the given headers.
   */
  public static <E> HttpEntity<E> createEntity(RequestHeaders headers) {
    return new HttpEntity<>(headers.toHeaders());
  }

  public static URI[] splitRequest(String url, RequestParameters queryParams, int maxUrlLength) {
    RequestParameters safeQueryParams = RequestParameters.init().setAll(queryParams);
    URI uri = createUri(url, safeQueryParams);

    if (uri.toString().length() > maxUrlLength) {
      Pair<RequestParameters, RequestParameters> split = safeQueryParams.split();

      if (null != split.getLeft() && null != split.getRight()) {
        URI[] left = splitRequest(url, split.getLeft(), maxUrlLength);
        URI[] right = splitRequest(url, split.getRight(), maxUrlLength);

        return Stream
                .concat(Arrays.stream(left), Arrays.stream(right))
                .distinct()
                .toArray(URI[]::new);
      }
    }

    return new URI[]{uri};
  }

  private static HttpHeaders createHeadersWithAuth(String token, String tokenPrefix) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, tokenPrefix + " " + token);
    return headers;
  }

}
