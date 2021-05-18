package org.vitrivr.cineast.api.rest.handlers.interfaces;

import io.javalin.http.Context;

/**
 * Special {@link PostRestHandler}, which automatically parses the body of the incoming request.
 *
 * @param <I> The input type for this handler. Incoming requests' body will be parsed as this type
 * @param <O> The output type of this handler. This handler will produce a result of this type.
 * @author loris.sauter
 * @version 1.1
 */
public interface ParsingPostRestHandler<I, O> extends PostRestHandler<O> {

  /**
   * {@inheritDoc}
   */
  @Override
  default O doPost(Context ctx) {
    return performPost(ctx.bodyAsClass(inClass()), ctx);
  }

  /**
   * Actually performs the post action.
   *
   * @param input The body of the request. For convenience already parsed
   * @param ctx   The request context
   * @return The resulting object
   */
  O performPost(I input, Context ctx);

  /**
   * Returns the input type as class
   *
   * @return The input type as class
   */
  Class<I> inClass();
}
