package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.util.Map;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.PostRestHandler;
import org.vitrivr.cineast.api.session.SessionManager;
import org.vitrivr.cineast.api.session.SessionType;

public class EndSessionHandler implements GetRestHandler<SessionState> {

    
    public static final String ID_NAME = "id";
    
    public static final String ROUTE = "session/end/:"+ID_NAME;
    
    @OpenApi(
        summary = "End the session for given id",
        path = ROUTE, method = HttpMethod.GET,
        pathParams = {
            @OpenApiParam(name = ID_NAME, description = "The id of the session to end")
        },
        tags = {"Session"},
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = SessionState.class))
        }
    )
    @Override
    public SessionState doGet(Context ctx) {
        final Map<String, String> parameters = ctx.pathParamMap();
        String sessionId = parameters.get(ID_NAME);
        if (sessionId == null) {
            sessionId = "";
        } else {
            SessionManager.endSession(sessionId);
        }
        return new SessionState(sessionId, -1, SessionType.UNAUTHENTICATED);
    }


    public String getDescription(RestHttpMethod method) {
        return "End the session for id";
    }

    @Override
    public Class<SessionState> outClass() {
        return SessionState.class;
    }
    
    @Override
    public String route() {
        return ROUTE;
    }
}
