package org.vitrivr.cineast.api.rest.handlers.actions.session;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.api.session.SessionType;
import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;

import java.util.Map;
import org.yaml.snakeyaml.events.Event.ID;

public class EndSessionHandler extends ParsingActionHandler<AnyMessage, SessionState> {

    public static final String ID_NAME = ":id";

    @Override
    public Object doGet(Map<String, String> parameters) {
        String sessionId = parameters.get(ID_NAME);
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

    @Override
    public String getRoute() {
        return "session/end/"+ ID_NAME;
    }

    @Override
    public String getDescription() {
        return "End the session for id";
    }

    @Override
    public Class<SessionState> outClass() {
        return SessionState.class;
    }
}
