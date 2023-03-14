package org.openlmis.integration.dhis2.exception;

import org.openlmis.integration.dhis2.util.Message;

/**
 * Signals user being unauthorized in external api.
 */
public class AuthenticationException extends BaseMessageException {
  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(Message message) {
    super(message);
  }
}
