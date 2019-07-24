package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.messages.session.SessionState;
import org.vitrivr.cineast.core.data.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.standalone.run.ExtractionItemContainer;

/**
 * @author silvan on 19.01.18.
 */
public class ExtractItemHandler extends ParsingActionHandler<ExtractionContainerMessage> {

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public Object doGet(Map<String, String> parameters) throws ActionHandlerException {
    throw new MethodNotSupportedException("HTTP GET is not supported for ExtractPathHandler");

  }

  @Override
  public Object doPost(ExtractionContainerMessage context, Map<String, String> parameters)
      throws ActionHandlerException {
    SessionState state = ValidateSessionHandler.validateSession(parameters); //TODO Use State

    LOGGER.debug("Received items {}", Arrays.toString(context.getItems()));
    SessionExtractionContainer.addPaths(context.getItems());
    return state;
  }

  @Override
  public Class<ExtractionContainerMessage> inClass() {
    return ExtractionContainerMessage.class;
  }
}
