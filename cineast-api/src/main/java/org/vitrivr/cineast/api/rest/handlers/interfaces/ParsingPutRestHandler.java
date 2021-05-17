package org.vitrivr.cineast.api.rest.handlers.interfaces;

import io.javalin.http.Context;

/**
 * Special {@link PutRestHandler}, which automatically parses the body of the incoming request.
 *
 * @param <I> The input type for this handler. Incoming requests' body will be parsed as this type
 * @param <O> The output type of this handler. This handler will produce a result of this type.
 *
 * @author loris.sauter
 * @version 1.1
 */
public interface ParsingPutRestHandler<I, O> extends PutRestHandler<O> {
  
  /**
   * {@inheritDoc}
   */
  @Override
  default O doPut(Context ctx) {
    return performPut(ctx.bodyAsClass(inClass()), ctx);
  }
  
  /**
   * Actually performs the put action.
   * @param inc The body of the request. For convenience already parsed
   * @param ctx The request context
   * @return The resulting object
   */
  O performPut(I inc, Context ctx);
  
  /**
   * Returns the input type as class
   * @return The input type as class
   */
  Class<I> inClass();
}
