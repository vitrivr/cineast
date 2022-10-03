package org.vitrivr.cineast.api.rest.handlers.actions.session;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.session.SessionMessage;
import org.vitrivr.cineast.api.rest.handlers.interfaces.PostRestHandler;

public class StartExtractionPostHandler implements PostRestHandler<SessionMessage> {

  public static final String ROUTE = "/extract/start";

  @Override
  public SessionMessage doPost(Context ctx) {
    if (SessionExtractionContainer.isOpen()) {
      return new SessionMessage("Extraction already running.");
    }
    SessionExtractionContainer.startExtraction();
    return new SessionMessage("Started extraction.");
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
          op.summary("Start a new extraction session");
          op.addTagsItem("Session");
          op.operationId("startExtraction");
        })
        .json("200", outClass());
  }
}
