package org.vitrivr.cineast.api.rest.handlers.actions.session;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.api.session.SessionType;

import java.util.Map;

public class EndSessionHandler implements GetRestHandler<SessionState> {
  
  
  public static final String ID_NAME = "id";
  
  public static final String ROUTE = "session/end/:" + ID_NAME;
  
  
  @Override
  public SessionState doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    String sessionId = parameters.get(ID_NAME);
    if (sessionId == null) {
      sessionId = "";
    } else {
      SessionManager.endSession(sessionId);
    }
    return new SessionState(sessionId, -1, SessionType.UNAUTHENTICATED);
  }
  
  
  @Override
  public Class<SessionState> outClass() {
    return SessionState.class;
  }
  
  @Override
  public String route() {
    return ROUTE;
  }
  
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("End the session for given id");
          op.description("Ends the session for the given id");
          op.addTagsItem("Session");
          op.operationId("endSession");
        })
        .pathParam(ID_NAME, String.class, p -> p.description("The id of the session to end"))
        .json("200", outClass());
  }
}
