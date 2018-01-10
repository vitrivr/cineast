package org.vitrivr.cineast.api.rest.handlers.abstracts;

import java.util.HashMap;
import java.util.Map;

import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ActionHandler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Request;
import spark.Response;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public abstract class ParsingActionHandler<A> implements ActionHandler<A> {

    /**
     * Jackson ObjectMapper used to map to/from objects.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /*
     * Can be used to setup the ObjectMapper.
     */
    static {
        MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
    }

    /**
     * Invoked when an incoming request is routed towards this class by Java Spark. The method handles
     * that request, extracts named parameters and parses the (optional) request body using Jackson. The
     * resulting context object is then forwarded to the invoke() method.
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return The content to be set in the response
     * @throws Exception implementation can choose to throw exception
     */
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Map<String, String> params = request.params();
        if (params == null) {
          params = new HashMap<>();
        }
        Object result = null;
        response.type("application/json");
        switch (request.requestMethod()) {
            case "GET":
            case "DELETE":
                result = this.invoke(null, params);
                break;
            case "POST":
            case "PUT":
                A parsedRequest = MAPPER.readValue(request.body(), this.inClass());
                result = this.invoke(parsedRequest, params);
                break;
            default:
                throw new MethodNotSupportedException(request);
        }
        return MAPPER.writeValueAsString(result);
    }
}
