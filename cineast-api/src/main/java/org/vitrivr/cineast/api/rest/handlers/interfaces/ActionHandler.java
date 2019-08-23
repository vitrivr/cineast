package org.vitrivr.cineast.api.rest.handlers.interfaces;

import java.util.Arrays;
import java.util.List;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import spark.Route;

import java.util.Map;
import spark.route.HttpMethod;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.01.17
 */
public interface ActionHandler<A> extends Route {
    /**
     * This method gets invoked by the router after a GET request has been processed. It parameters and does something meaningful with it.
     * Once processing has completed, the method returns a resulting object.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return Object resulting from the invocation.
     * @throws ActionHandlerException If an error occurs during invocation.
     */
    Object doGet(Map<String,String> parameters) throws ActionHandlerException;

    /**
     * This method gets invoked by the router after a DELETE request has been processed. It takes parameters and does something meaningful with it.
     * Once processing has completed, the method returns a resulting object or null.
     *
     * @param parameters Map containing named parameters in the URL.
     * @throws ActionHandlerException If an error occurs during invocation.
     */
    default void doDelete(Map<String,String> parameters) throws ActionHandlerException {
        throw new MethodNotSupportedException("HTTP DELETE method is not supported by '" + this.getClass().getSimpleName() + "'.");
    }

    /**
     * This method gets invoked by the router after a POST request has been processed. It takes an optional context object and parameters and does
     * something meaningful with it. Once processing has completed, the method returns a resulting object.
     *
     * @param context Object that is handed to the invocation, usually parsed from the request body. May be NULL!
     * @param parameters Map containing named parameters in the URL.
     * @return Object resulting from the invocation.
     * @throws ActionHandlerException If an error occurs during invocation.
     */
    default Object doPost(A context, Map<String,String> parameters) throws ActionHandlerException {
        throw new MethodNotSupportedException("HTTP POST method is not supported by '" + this.getClass().getSimpleName() + "'.");
    }

    /**
     * This method gets invoked by the router after a POST request has been processed. It takes an optional context object and parameters and does
     * something meaningful with it. Once processing has completed, the method returns a resulting object.
     *
     * @param context Object that is handed to the invocation, usually parsed from the request body. May be NULL!
     * @param parameters Map containing named parameters in the URL.
     * @return Object resulting from the invocation.
     * @throws ActionHandlerException If an error occurs during invocation.
     */
    default Object doPut(A context, Map<String,String> parameters) throws ActionHandlerException {
        throw new MethodNotSupportedException("HTTP PUT method is not supported by '" + this.getClass().getSimpleName() + "'.");
    }

    /**
     * Returns the supported {@link HttpMethod}s by this ActionHandler.
     *
     * Please be aware that {@link HttpMethod} by spark is a superset of the official HTTP methods.
     * However, {@link ActionHandler} only supports a subset of them:
     * <ul>
     *   <li>{@link HttpMethod#get}</li>
     *   <li>{@link HttpMethod#post}</li>
     *   <li>{@link HttpMethod#delete}</li>
     *   <li>{@link HttpMethod#put}</li>
     * </ul>
     *
     * @return The supported HTTP methods. Can also be used to control which routes are registered
     */
    default List<HttpMethod> supportedMethods(){
        return Arrays.asList(HttpMethod.get);
    }

    /**
     * Must return the class of type A. Required due to type-erasure.
     *
     * @return Class of type A (A.class)
     */
     Class<A> inClass();
}
