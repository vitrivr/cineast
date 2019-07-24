package org.vitrivr.cineast.api.websocket.handlers.abstracts;

/**
 * This abstract class implements the WebsocketMessageHandler interface. The assumption for classes extending this class
 * is that they are statelsse. Hence, the same instance of the message handler can be used to handle messages from
 * different sessions.
 *
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public abstract class StatelessWebsocketMessageHandler<A> extends AbstractWebsocketMessageHandler<A> {
    /**
     * Indicates whether the WebsocketMessageHandler is stateless or not.
     *
     * @return True if WebsocketMessageHandler is stateless, false otherwise.
     */
    @Override
    public final boolean isStateless() {
        return true;
    }
}
