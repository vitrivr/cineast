package org.vitrivr.cineast.api.rest.handlers.actions.session;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;

public class ExtractItemHandler implements ParsingPostRestHandler<ExtractionContainerMessage, SessionState> {

  public static final String ROUTE = "session/extract/new";
  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public SessionState performPost(ExtractionContainerMessage context, Context ctx) {
    SessionState state = ValidateSessionHandler.validateSession(ctx.pathParamMap()); //TODO Use State

    LOGGER.debug("Received items {}", Arrays.toString(context.getItemsAsArray()));
    SessionExtractionContainer.addPaths(context.getItemsAsArray());
    return state;
  }

  @Override
  public Class<ExtractionContainerMessage> inClass() {
    return ExtractionContainerMessage.class;
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
          op.operationId("extractItem");
          op.summary("Extract new item");
          op.description("TODO");
          op.addTagsItem("Session");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
