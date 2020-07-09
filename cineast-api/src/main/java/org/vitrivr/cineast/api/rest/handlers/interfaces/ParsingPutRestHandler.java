package org.vitrivr.cineast.api.rest.handlers.interfaces;

import io.javalin.http.Context;

public interface ParsingPutRestHandler<I, O> extends PutRestHandler<O> {
  
  @Override
  default O doPut(Context ctx) {
    return performPut(ctx.bodyAsClass(inClass()), ctx);
  }
  
  O performPut(I inc, Context ctx);
  
  Class<I> inClass();
}
