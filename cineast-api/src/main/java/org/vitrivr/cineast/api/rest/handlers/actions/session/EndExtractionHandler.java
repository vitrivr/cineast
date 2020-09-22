package org.vitrivr.cineast.api.rest.handlers.actions.session;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.handlers.interfaces.PostRestHandler;

/**
 * @author silvan on 23.01.18.
 */
public class EndExtractionHandler implements PostRestHandler<SessionState> {
  
  // FIXME This needs cleanup and testing
  
  
  public static final String ROUTE = "session/extract/end";
  
  
  @Override
  public SessionState doPost(Context ctx) {
    SessionState state = ValidateSessionHandler.validateSession(ctx.pathParamMap()); //TODO Use State
    SessionExtractionContainer.endSession();
    return state;
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
          op.summary("End the active extraction session");
          op.description("CAUTION. Untested");
          op.deprecated(true); // FIXME when testing / cleanup is done, please remove
          op.addTagsItem("Session");
          op.operationId("endExtraction");
        })
        .json("200", outClass());
  }
}
