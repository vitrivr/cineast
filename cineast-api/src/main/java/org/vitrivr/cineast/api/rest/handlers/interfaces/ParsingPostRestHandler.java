package org.vitrivr.cineast.api.rest.handlers.interfaces;

import io.javalin.http.Context;

/**
 * Specialised {@link PostRestHandler}, which parses its body before performing the POST operation.
 * @param <I>
 * @param <O>
 */
public interface ParsingPostRestHandler<I,O> extends PostRestHandler<O> {
  
  
  @Override
  default O doPost(Context ctx){
    return performPost(ctx.bodyAsClass(inClass()), ctx);
  }
  
  /**
   * Actually performs the post action.
   * @param input The body of the request. For convenience already parsed
   * @param ctx The request context
   * @return The resulting object
   */
  O performPost(I input, Context ctx);
  
  Class<I> inClass();
}
