package org.vitrivr.cineast.api.rest.handlers.interfaces;

import org.vitrivr.cineast.api.rest.RestHttpMethod;

import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;

/**
 * A documented {@link ActionHandler}, ready to be used with {@link io.github.manusant.ss.rest.Endpoint}.
 *
 * @author loris.sauter
 */
public interface DocumentedRestOperation<A,O> extends ActionHandler<A>, Handler {

    /**
     * The path of the route a.k.a the operation's name
     * @return The path / route of the operation
     */
    String getRoute();

    /**
     * Dedicated GET route, if any
     * Defaults to {@link #getRoute()}
     */
    default String routeForGet(){
        return getRoute();
    }

    /**
     * Dedicated POST route, if any
     * Defaults to {@link #getRoute()}
     */
    default String routeForPost(){
        return getRoute();
    }

    /**
     * Dedicated DELETE route, if any
     * Defaults to {@link #getRoute()}
     */
    default String routeForDelete(){
        return getRoute();
    }

    /**
     * Dedicated PUT route, if any
     * Defaults to {@link #getRoute()}
     */
    default String routeForPut(){
        return getRoute();
    }

    /**
     * A check whether the response is a collection.
     * If so, {@link #outClass()} returns the collection's item type
     * @return {@code true} if and only if this operation returns a collection and hence {@link #outClass()}
     * @deprecated This will be removed in a future update, as some clients might not be able to properly handle top-level-collections. Hence, wrapper objects will be introduced.
     */
    @Deprecated
    default boolean isResponseCollection(){
        return false;
    }

    /**
     * The response type's class.
     * Is required to properly set the response type.
     *
     * Similar to {@link ActionHandler#inClass()}, except for out
     *
     * @return The class of the response, O. O.class
     */
    public Class<?> outClass();

    /**
     * Gets the OpenAPI operation description for the specified HTTP method of this handler.
     * 
     * @param method HTTP method to be described
     * @return
     */
    public String getDescription(RestHttpMethod method);

    /**
     * Gets the OpenAPI operation summary for the specified HTTP method of this handler.
     * By default this is the same as {@link #getDescription(RestHttpMethod)}.
     * 
     * @param method HTTP method to be summarized
     * @return
     */
    public default String getSummary(RestHttpMethod method) {
        return this.getDescription(method);
    }

    /**
     * Decorates the OpenAPI documentation for this RESTful operation.
     * The following values are set automatically but can be overwritten:
     * <ul>
     * <li>Operation summary</li>
     * <li>Operation description</li>
     * <li>Operation ID</li>
     * <li>Request type (except for GET HTTP method)</li>
     * <li>Response type</li>
     * </ul>
     * 
     * @param method HTTP method to be documented
     * @param document the OpenAPI documentation to be decorated
     * @return An {@link OpenApiDocumentation}
     */
    default void document(RestHttpMethod method, OpenApiDocumentation document) {

    }
}
