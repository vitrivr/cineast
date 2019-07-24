package org.vitrivr.cineast.api.rest.handlers.actions.session;

import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;

import java.util.Map;

/**
 * @author silvan on 23.01.18.
 */
public class EndExtractionHandler extends ParsingActionHandler<AnyMessage> {

  @Override
  public Object doGet(Map<String, String> parameters) throws ActionHandlerException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object doPost(AnyMessage context, Map<String, String> parameters)
      throws ActionHandlerException {
    SessionState state = ValidateSessionHandler.validateSession(parameters); //TODO Use State

    SessionExtractionContainer.endSession();
    return state;
  }

  @Override
  public Class<AnyMessage> inClass() {
    return AnyMessage.class;
  }
}
