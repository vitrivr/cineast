package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Map;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.data.messages.session.SessionState;

public class ValidateSessionHandler extends ParsingActionHandler<AnyMessage> {

  @Override
  public Object invoke(AnyMessage context, Map<String, String> parameters)
      throws ActionHandlerException {

    String sessionId = parameters.get(":id");
    if(sessionId == null){
      return new SessionState("", -1);
    }
    
    Session s = SessionManager.get(sessionId);
    
    if(s == null || !s.isValid()){
      return new SessionState(sessionId, -1);
    }
    
    //if session is valid: extend life time
    s.setLifeTime(60 * 60 * 24); //TODO move life time to config
    
    return new SessionState(s);
  }

  @Override
  public Class<AnyMessage> inClass() {
    return AnyMessage.class;
  }

}
