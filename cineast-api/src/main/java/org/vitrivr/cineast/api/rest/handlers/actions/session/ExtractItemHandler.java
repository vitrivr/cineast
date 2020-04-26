package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;

/**
 * @author silvan on 19.01.18.
 */
public class ExtractItemHandler extends ParsingActionHandler<ExtractionContainerMessage, SessionState> {

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public Object doGet(Map<String, String> parameters) throws ActionHandlerException {
    throw new MethodNotSupportedException("HTTP GET is not supported for ExtractPathHandler");

  }

  @Override
  public Object doPost(ExtractionContainerMessage context, Map<String, String> parameters)
      throws ActionHandlerException {
    SessionState state = ValidateSessionHandler.validateSession(parameters); //TODO Use State

    LOGGER.debug("Received items {}", Arrays.toString(context.getItemsAsArray()));
    SessionExtractionContainer.addPaths(context.getItemsAsArray());
    return state;
  }

  @Override
  public Class<ExtractionContainerMessage> inClass() {
    return ExtractionContainerMessage.class;
  }

  @Override
  public String getRoute() {
    return "session/extract/new";
  }

  @Override
  public String getDescription(RestHttpMethod method) {
    return "Extract new item";
  }

  @Override
  public Class<SessionState> outClass() {
    return SessionState.class;
  }

  @Override
  public List<RestHttpMethod> supportedMethods() {
    return Collections.singletonList(RestHttpMethod.POST);
  }
}
