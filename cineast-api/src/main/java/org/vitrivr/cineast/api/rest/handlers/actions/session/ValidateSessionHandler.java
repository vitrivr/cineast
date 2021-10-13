package org.vitrivr.cineast.api.rest.handlers.actions.session;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Map;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.api.session.SessionType;

public class ValidateSessionHandler implements GetRestHandler<SessionState> {

  private final static String ID_NAME = "id";

  public static final String ROUTE = "session/validate/{" + ID_NAME+"}";

  public static SessionState validateSession(String sessionId) {
    if (sessionId == null) {
      return new SessionState("", -1, SessionType.UNAUTHENTICATED);
    }
    final Session s = SessionManager.get(sessionId);

    if (s == null || !s.isValid()) {
      return new SessionState(sessionId, -1, SessionType.UNAUTHENTICATED);
    }
    //if session is valid: extend life time
    s.setLifeTime(60 * 60 * 24); //TODO move life time to config
    return new SessionState(s);
  }

  public static SessionState validateSession(Map<String, String> parameters) {
    final String sessionId = parameters.get(ID_NAME);
    return validateSession(sessionId);
  }

  @Override
  public SessionState doGet(Context ctx) {
    return validateSession(ctx.pathParamMap());
  }

  public String getDescription(RestHttpMethod method) {
    return "Validate the session with the given id";
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
          op.summary("Validates the session with given id");
          op.operationId("validateSession");
          op.addTagsItem("Session");
        })
        .pathParam(ID_NAME, String.class, p -> p.description("The id to validate the session of"))
        .json("200", outClass());
  }
}
