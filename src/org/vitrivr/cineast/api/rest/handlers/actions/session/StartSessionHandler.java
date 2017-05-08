package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Map;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.core.data.messages.session.SessionState;
import org.vitrivr.cineast.core.data.messages.session.StartSessionMessage;

public class StartSessionHandler extends ParsingActionHandler<StartSessionMessage> {

  @Override
  public SessionState invoke(StartSessionMessage context, Map<String, String> parameters)
      throws ActionHandlerException {
    
    //TODO check credentials
    
    Session s = SessionManager.newSession(60 * 60 * 24); //TODO move life time to config
    
    return new SessionState(s);
  }

  @Override
  public Class<StartSessionMessage> inClass() {
    return StartSessionMessage.class;
  }

}
