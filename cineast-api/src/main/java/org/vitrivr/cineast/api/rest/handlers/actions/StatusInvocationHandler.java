package org.vitrivr.cineast.api.rest.handlers.actions;


import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.general.Ping;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;

public class StatusInvocationHandler implements GetRestHandler<Ping> {

  public static final String ROUTE = "status";


  @Override
  public Ping doGet(Context ctx) {
    return new Ping();
  }


  public String getDescription(RestHttpMethod method) {
    return "Get the status of the server";
  }

  @Override
  public Class<Ping> outClass() {
    return Ping.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Get the status of the server");
          op.operationId("status");
          op.addTagsItem("Status");
        })
        .json("200", outClass());
  }
}
