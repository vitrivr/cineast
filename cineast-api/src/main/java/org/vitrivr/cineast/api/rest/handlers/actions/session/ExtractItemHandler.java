package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.KEY_NAME;

/**
 * @author silvan on 19.01.18.
 */
public class ExtractItemHandler implements ParsingPostRestHandler<ExtractionContainerMessage, SessionState> {

  private static final Logger LOGGER = LogManager.getLogger();
  
  public static final String ROUTE = "session/extract/new";
  
  @OpenApi(
      summary = "Extrat new item",
      path = ROUTE, method = HttpMethod.POST,
      requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ExtractionContainerMessage.class)),
      tags = {"Session"},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = SessionState.class))
      }
  )
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
}
