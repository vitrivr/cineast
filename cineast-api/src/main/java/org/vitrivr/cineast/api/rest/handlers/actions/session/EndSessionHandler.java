package org.vitrivr.cineast.api.rest.handlers.actions.session;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.api.session.SessionType;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.data.messages.session.SessionState;

import java.util.Map;

public class EndSessionHandler extends ParsingActionHandler<AnyMessage> {

    @Override
    public Object doGet(Map<String, String> parameters) {
        String sessionId = parameters.get(":id");
        if (sessionId == null) {
            sessionId = "";
        } else {
            SessionManager.endSession(sessionId);
        }
        return new SessionState(sessionId, -1, SessionType.UNAUTHENTICATED);
    }

    @Override
    public Class<AnyMessage> inClass() {
        return AnyMessage.class;
    }

}
