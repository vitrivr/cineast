package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Map;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.data.messages.session.SessionState;

public class EndSessionHandler extends ParsingActionHandler<AnyMessage> {

  @Override
  public Object invoke(AnyMessage context, Map<String, String> parameters)
      throws ActionHandlerException {

    String sessionId = parameters.get(":id");
    if(sessionId == null){
      sessionId = "";
    }else{
      SessionManager.endSession(sessionId);
    }
    
    return new SessionState(sessionId, -1);
  }

  @Override
  public Class<AnyMessage> inClass() {
    return AnyMessage.class;
  }

}
