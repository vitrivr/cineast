package org.vitrivr.cineast.api.rest.handlers.interfaces;

/**
 * A documented {@link ActionHandler}, ready to be used with {@link io.github.manusant.ss.rest.Endpoint}.
 *
 * @author loris.sauter
 */
public interface DocumentedRestOperation<A,O> extends ActionHandler<A> {

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
   * An universal description of the operation
   * @return The operation's description
   */
  String getDescription();

  /**
   * The description for the operation's GET method
   */
  default String descriptionForGet(){
    return getDescription();
  }

  /**
   * The description for the operation's POST method
   */
  default String descriptionForPost(){
    return getDescription();
  }

  /**
   * The description for the operation's DELETE method
   */
  default String descriptionForDelete(){
    return getDescription();
  }

  /**
   * The description for the operation's PUT method
   */
  default String descriptionForPut(){
    return getDescription();
  }
  /**
   * The response type's class.
   * Is required to properly set the response type.
   *
   * Similar to {@link ActionHandler#inClass()}, except for out
   *
   * @return The class of the response, O. O.class
   */
  Class<O> outClass();

  /**
   * A check whether the response is a collection.
   * If so, {@link #outClass()} returns the collection's item type
   * @return {@code true} if and only if this operation returns a collection and hence {@link #outClass()}
   */
  default boolean isResponseCollection(){
    return false;
  }

}
