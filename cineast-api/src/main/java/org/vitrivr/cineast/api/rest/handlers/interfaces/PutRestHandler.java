package org.vitrivr.cineast.api.rest.handlers.interfaces;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

/**
 * {@link DocumentedRestHandler} for PUT operations.
 * <p>
 * The handler's {@link #doPut(Context)} will produce a result of the specified type {@code T}, which the REST API will send as JSON.
 *
 * <h2>Architecture</h2>
 * Ultimately, this is a {@link io.javalin.http.Handler} whose {@link io.javalin.http.Handler#handle(Context)} method is overridden, such that this handler's {@link #put(Context)} is called, which will write the result of this handler's {@link #doPut(Context)} as JSON to the context.
 *
 * @param <T> The type the handler will return.
 */
public interface PutRestHandler<T> extends DocumentedRestHandler {

  /**
   * {@inheritDoc}
   */
  @Override
  default void handle(@NotNull Context ctx) throws Exception {
    put(ctx);
  }

  /**
   * Performs the PUT REST operation of this handler and sends the result to the requester. Exception handling has to be done by the caller.
   */
  default void put(Context ctx) {
    ctx.json(doPut(ctx));
  }

  /**
   * Implementation of the actual GET REST operation of this handler
   *
   * @param ctx The context of the request
   * @return The result as an object, ready to send to the requester
   */
  T doPut(Context ctx);

  /**
   * Returns the resulting type of this handler as class.
   *
   * @return The class of the result type of this handler
   */
  Class<T> outClass();
}
