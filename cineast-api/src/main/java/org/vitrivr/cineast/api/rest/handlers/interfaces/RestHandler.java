package org.vitrivr.cineast.api.rest.handlers.interfaces;

import io.javalin.http.Handler;

/**
 * A {@link Handler} with an associated route.
 */
public interface RestHandler extends Handler {

  /**
   * Returns the route of this handler. Usually, the route is prefixed with some API information (e.g. {@code api/v1/}) to ultimately form the handler's address
   *
   * @return The route of this handler, including path parameters prefixed with {@code :} (colon).
   */
  String route();
}
