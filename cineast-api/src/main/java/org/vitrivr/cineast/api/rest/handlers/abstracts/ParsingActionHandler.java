package org.vitrivr.cineast.api.rest.handlers.abstracts;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.handlers.interfaces.DocumentedRestOperation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.http.Context;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public abstract class ParsingActionHandler<A,O> implements DocumentedRestOperation<A,O> {
    
    public static final String CONTENT_TYPE = "application/json";

    /**
     * Jackson ObjectMapper used to map to/from objects.
     */
    public static final ObjectMapper MAPPER = new ObjectMapper();

    /* Can be used to setup the ObjectMapper.  */
    static {
        MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
    }
    
    /**
     * Invoked when an incoming request is routed towards this class by Javalin. The method handles
     * that request, extracts named parameters and parses the (optional) request body using Jackson. The
     * resulting context object is then forwarded to the doGet() method.
     *
     * @return The content to be set in the response
     * @throws Exception implementation can choose to throw exception
     */
    @Override
    public void handle(Context ctx) throws Exception {
    	Map<String, String> params = ctx.pathParamMap();
        if (params == null) {
            params = new HashMap<>();
        }
        switch (ctx.method()) {
            case "GET":
                ctx.json(this.doGet(params));
                break;
            case "DELETE":
                this.doDelete(params);
                break;
            case "POST":
                ctx.json(this.doPost(ctx.bodyAsClass(this.inClass()), params));
                break;
            case "PUT":
                ctx.json(this.doPut(ctx.bodyAsClass(this.inClass()), params));
                break;
            default:
                throw new MethodNotSupportedException(ctx);
        }
    }

    @Override
    public List<RestHttpMethod> supportedMethods() {
        return Collections.singletonList(RestHttpMethod.GET);
    }
}
