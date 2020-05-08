package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Map;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.messages.session.StartSessionMessage;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.api.session.SessionType;

public class ValidateSessionHandler implements GetRestHandler<SessionState> {
  
  private final static String ID_NAME = "id";
  
  public static final String ROUTE = "session/validate/:"+ID_NAME;
  
  
  @OpenApi(
      summary = "Validates the session with given id",
      path = ROUTE, method = HttpMethod.GET,
      pathParams = {
        @OpenApiParam(name = ID_NAME, description = "The id to validate the session of")
      },
      tags = {"Session"},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = SessionState.class))
      }
  )
  @Override
  public SessionState doGet(Context ctx) {
    return validateSession(ctx.pathParamMap());
  }

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
}
