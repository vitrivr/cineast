package org.vitrivr.cineast.api.rest.handlers.actions.session;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.credentials.Credentials;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.messages.session.StartSessionMessage;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.session.CredentialManager;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.api.session.SessionType;

public class StartSessionHandler implements ParsingPostRestHandler<StartSessionMessage, SessionState> {
  
  public static final String ROUTE = "session/start";
  
  @Override
  public SessionState performPost(StartSessionMessage context, Context ctx) {
    SessionType type = SessionType.UNAUTHENTICATED;
    if (context != null) {
      Credentials credentials = context.getCredentials();
      type = CredentialManager.authenticate(credentials);
    }
    Session s = SessionManager.newSession(60 * 60 * 24, type); //TODO move life time to config
    return new SessionState(s);
  }
  
  @Override
  public Class<StartSessionMessage> inClass() {
    return StartSessionMessage.class;
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
          op.summary("Start new session for given credentials");
          op.operationId("startSession");
          op.addTagsItem("Session");
        })
        .body(inClass())
        .json("200",outClass());
  }
}
