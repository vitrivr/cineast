package org.vitrivr.cineast.api.rest.handlers.basic;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import spark.Route;

import java.util.Map;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.01.17
 */
public interface ActionHandler<A> extends Route {
    /**
     * This method gets invoked by the router after the raw request has been processed. It takes an optional context
     * object and parameters and does something meaningful with it. Once processing has completed, the method
     * returns a resulting object.
     *
     * @param context Object that is handed to the invocation, usually parsed from the request body. May be NULL!
     * @param parameters Map containing named parameters in the URL.
     * @return Object resulting from the invocation.
     * @throws ActionHandlerException If an error occurs during invocation.
     */
    public Object invoke(A context, Map<String,String> parameters) throws ActionHandlerException;

    /**
     * Must return the class of type A. Required due to type-erasure.
     *
     * @return Class of type A (A.class)
     */
    public Class<A> inClass();
}
