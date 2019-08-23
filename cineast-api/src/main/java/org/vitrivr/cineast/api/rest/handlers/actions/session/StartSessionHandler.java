package org.vitrivr.cineast.api.rest.handlers.actions.session;

import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.session.CredentialManager;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.api.session.SessionType;
import org.vitrivr.cineast.api.messages.credentials.Credentials;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.messages.session.StartSessionMessage;

import java.util.Map;
import spark.route.HttpMethod;

public class StartSessionHandler extends ParsingActionHandler<StartSessionMessage, SessionState> {

    {
        // ONLY method POST
        supportedHttpMethods.clear();
        supportedHttpMethods.add(HttpMethod.post);
    }

    /**
     * Processes a HTTP GET request. Always throws a {@link MethodNotSupportedException}
     *
     * @param parameters Map containing named parameters in the URL.
     * @throws MethodNotSupportedException Always
     */
    public Object doGet(Map<String, String> parameters) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("HTTP GET is not supported for StartSessionHandler.");
    }

    @Override
    public SessionState doPost(StartSessionMessage context, Map<String, String> parameters) {
        SessionType type = SessionType.UNAUTHENTICATED;
        if (context != null) {
            Credentials credentials = context.getCredentials();
            type = CredentialManager.authenticate(credentials);
        }
        Session s = SessionManager.newSession(60 * 60 * 24, type); //TODO move life time to config
        return new SessionState(s);
    }

    @Override
    public Class<StartSessionMessage> inClass() {
        return StartSessionMessage.class;
    }

    @Override
    public String getRoute() {
        return "session/start";
    }

    @Override
    public String getDescription() {
        return "Start a new session for given credentials";
    }

    @Override
    public Class<SessionState> outClass() {
        return SessionState.class;
    }
}
