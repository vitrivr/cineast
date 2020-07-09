package org.vitrivr.cineast.api.rest.handlers.interfaces;

import io.javalin.http.Context;

public interface DeleteRestHandler<T> extends RestHandler {
  
  /**
   * Performs the POST REST operation of this handler and sends the result to the requester.
   * Exception handling has to be done by the caller.
   *
   */
  default void delete(Context ctx){
    ctx.json(doDelete(ctx));
  }
  
  /**
   * Implementation of the actual GET REST operation of this handler
   * @param ctx The context of the request
   * @return The result as an object, ready to send to the requester
   */
  T doDelete(Context ctx);
  
  Class<T> outClass();
}
