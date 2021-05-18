package org.vitrivr.cineast.api.rest.exceptions;

import io.javalin.http.Context;

public class MethodNotSupportedException extends ActionHandlerException {

  private static final long serialVersionUID = 4872163795505943837L;

  public MethodNotSupportedException(String message) {
    super(message);
  }

  public MethodNotSupportedException(Context request) {
    this("HTTP method '" + request.method() + "' is not supported for call to' " + request.url() + "'.");
  }
}
