package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Map;

import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.api.session.SessionType;

public class ValidateSessionHandler extends ParsingActionHandler<AnyMessage, SessionState> {

  @Override
  public Object doGet(Map<String, String> parameters) {
    return validateSession(parameters);
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

  @Override
  public Class<AnyMessage> inClass() {
    return AnyMessage.class;
  }

  public static SessionState validateSession(Map<String, String> parameters) {
    final String sessionId = parameters.get(":id");
    return validateSession(sessionId);
  }

  @Override
  public String getRoute() {
    return "session/validate/:id";
  }

  @Override
  public String getDescription(RestHttpMethod method) {
    return "Validate the session with the given id";
  }

  @Override
  public Class<SessionState> outClass() {
    return SessionState.class;
  }
}
