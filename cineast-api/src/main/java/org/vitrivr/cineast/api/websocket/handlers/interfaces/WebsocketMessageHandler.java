package org.vitrivr.cineast.api.websocket.handlers.interfaces;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;

/**
 * Interface for a WebsocketMessageHandler handler that handles a message of type A (type variable).
 *
 * The assumption behind this class is that another class routes incoming WebSocket messages towards the correct
 * WebsocketMessageHandler instance. That instance then carries out all the necessary steps to create an appropriate
 * response for the incoming message and sends that response to the source of the message.
 *
 * @see Message
 *
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public interface WebsocketMessageHandler<A>  {

    /**
     * Invoked when a Message of type A arrives and requires handling.
     *
     * @param session WebSocketSession for which the message arrived.
     * @param message Message of type a that needs to be handled.
     */
    void handle(Session session, A message);

    /**
     * Indicates whether the WebsocketMessageHandler is stateless or not.
     *
     * @return True if WebsocketMessageHandler is stateless, false otherwise.
     */
    boolean isStateless();
}
