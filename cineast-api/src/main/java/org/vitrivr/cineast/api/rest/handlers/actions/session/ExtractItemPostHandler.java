package org.vitrivr.cineast.api.rest.handlers.actions.session;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.api.messages.session.SessionMessage;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;

public class ExtractItemPostHandler implements ParsingPostRestHandler<ExtractionContainerMessage, SessionMessage> {

  public static final String ROUTE = "extract/new";

  @Override
  public SessionMessage performPost(ExtractionContainerMessage message, Context ctx) {
    if (!SessionExtractionContainer.isOpen()) {
      return new SessionMessage("Extraction not running.");
    }
    SessionExtractionContainer.addPaths(message.items());
    return new SessionMessage("Queued new item for extraction.");
  }

  @Override
  public Class<ExtractionContainerMessage> inClass() {
    return ExtractionContainerMessage.class;
  }

  @Override
  public Class<SessionMessage> outClass() {
    return SessionMessage.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Extract new item");
          op.addTagsItem("Session");
          op.operationId("extractItem");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
