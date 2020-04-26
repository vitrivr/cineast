package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;

/**
 * @author silvan on 23.01.18.
 */
public class StartExtractionHandler extends ParsingActionHandler<AnyMessage, SessionState> {

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public List<RestHttpMethod> supportedMethods() {
    return Collections.singletonList(RestHttpMethod.POST);
  }

  @Override
  public Object doGet(Map<String, String> parameters) throws ActionHandlerException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object doPost(AnyMessage context, Map<String, String> parameters)
      throws ActionHandlerException {
    SessionState state = ValidateSessionHandler.validateSession(parameters); //TODO Use State

    if (SessionExtractionContainer.keepAliveCheckIfClosed()) {
      LOGGER.info("Session is closed, restarting");
      SessionExtractionContainer.restartExceptCounter();
      return state;
    }
    LOGGER.debug("Session already open, only sent keepAlive message");
    return state;
  }

  @Override
  public Class<AnyMessage> inClass() {
    return AnyMessage.class;
  }

  @Override
  public String getRoute() {
    return "session/extract/start";
  }

  @Override
  public String getDescription(RestHttpMethod method) {
    return "Start extraction session";
  }

  @Override
  public Class<SessionState> outClass() {
    return SessionState.class;
  }
}
