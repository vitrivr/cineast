package org.vitrivr.cineast.api.rest.handlers.actions.session;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.interfaces.PostRestHandler;

/**
 * @author silvan on 23.01.18.
 */
public class StartExtractionHandler implements PostRestHandler<SessionState> {

  // FIXME this needs cleanup / rework
  
  private static final Logger LOGGER = LogManager.getLogger();

  public static final String ROUTE = "session/extract/start";
  
  
  @OpenApi(
      summary = "Start extraction session",
      path = ROUTE, method = HttpMethod.POST,
      tags = {"Session"},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = SessionState.class))
      }
  )
  @Override
  public SessionState doPost(Context ctx) {
    SessionState state = ValidateSessionHandler.validateSession(ctx.pathParamMap()); //TODO Use State

    if (SessionExtractionContainer.keepAliveCheckIfClosed()) {
      LOGGER.info("Session is closed, restarting");
      SessionExtractionContainer.restartExceptCounter();
      return state;
    }
    LOGGER.debug("Session already open, only sent keepAlive message");
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
          op.summary("Start extraction session");
          op.description("Changes the session's state to extraction");
          op.addTagsItem("Session");
          op.deprecated(true); // FIXME remove when done
          op.operationId("startExtraction");
        })
        .json("200", outClass());
  }
}
