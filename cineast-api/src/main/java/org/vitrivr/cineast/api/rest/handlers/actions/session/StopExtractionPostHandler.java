package org.vitrivr.cineast.api.rest.handlers.actions.session;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.session.SessionMessage;
import org.vitrivr.cineast.api.rest.handlers.interfaces.PostRestHandler;

public class StopExtractionPostHandler implements PostRestHandler<SessionMessage> {

  public static final String ROUTE = "extract/stop";

  @Override
  public SessionMessage doPost(Context ctx) {
    if (!SessionExtractionContainer.isOpen()) {
      return new SessionMessage("Extraction not running.");
    }
    SessionExtractionContainer.stopExtraction();
    return new SessionMessage("Stopped extraction.");
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
          op.summary("Stop the active extraction session");
          op.addTagsItem("Session");
          op.operationId("stopExtraction");
        })
        .json("200", outClass());
  }
}
