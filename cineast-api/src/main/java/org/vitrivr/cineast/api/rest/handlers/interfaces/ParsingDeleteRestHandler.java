package org.vitrivr.cineast.api.rest.handlers.interfaces;

import io.javalin.http.Context;

public interface ParsingDeleteRestHandler<I,O> extends DeleteRestHandler<O> {
  
  @Override
  default O doDelete(Context ctx){
    return performDelete(ctx.bodyAsClass(inClass()), ctx);
  }
  
  O performDelete(I in, Context ctx);
  
  Class<I> inClass();
}
