package org.vitrivr.cineast.api.rest.handlers.actions;


import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import org.vitrivr.cineast.api.messages.general.Ping;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.01.17
 */
public class StatusInvokationHandler implements GetRestHandler<Ping> {
  
  public static final String ROUTE = "status";
  
  
  @OpenApi(
      summary = "Get the status of the server",
      path = ROUTE, method = HttpMethod.GET,
      responses = @OpenApiResponse(status = "200", content = @OpenApiContent(from = Ping.class)),
      tags = {"Status"}
  )
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
}
