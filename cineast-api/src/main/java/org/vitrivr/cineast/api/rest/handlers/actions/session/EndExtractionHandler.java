package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.PostRestHandler;

/**
 * @author silvan on 23.01.18.
 */
public class EndExtractionHandler implements PostRestHandler<SessionState> {

  public static final String ROUTE = "session/extract/end";
  

  @OpenApi(
      summary = "End the active extraction session",
      path= ROUTE, method = HttpMethod.POST,
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from=SessionState.class))
      },
      tags={"Session"}
  )
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
}
