package org.vitrivr.cineast.api.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.vitrivr.cineast.api.websocket.handlers.MetadataLookupMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.StatusMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.interfaces.WebsocketMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.MoreLikeThisQueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.NeighbouringQueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.SimilarityQueryMessageHandler;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;


/**
 * This class establishes a WebSocket endpoint listening on the specified port. Incoming messages are expected
 * to come in a JSON format and to have an application specific POJO representation. This class maps those messages
 * to such a POJO and routes them towards a WebsocketMessageHandler provided that such a handler has been registered.
 *
 * @see WebsocketMessageHandler
 * @see Message
 *
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
@WebSocket
public class WebsocketAPI {

    /** Logging facility. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Store SESSIONS if you want to, for example, broadcast a message to all users.*/
    private static final Queue<Session> SESSIONS = new ConcurrentLinkedQueue<>();

    /** List of stateless {@link WebsocketMessageHandler} classes for the API. */
    private static final HashMap<MessageType, WebsocketMessageHandler<?>> STATELESS_HANDLERS = new HashMap<>();
    static {
        STATELESS_HANDLERS.put(MessageType.Q_SIM, new SimilarityQueryMessageHandler());
        STATELESS_HANDLERS.put(MessageType.Q_MLT, new MoreLikeThisQueryMessageHandler());
        STATELESS_HANDLERS.put(MessageType.Q_NESEG, new NeighbouringQueryMessageHandler());
        STATELESS_HANDLERS.put(MessageType.PING, new StatusMessageHandler());
        STATELESS_HANDLERS.put(MessageType.M_LOOKUP, new MetadataLookupMessageHandler());
    }

    /* */
    private JacksonJsonProvider reader = new JacksonJsonProvider();
 
    /**
     * Invoked whenever a new connection is established. Configures the session and stashes it in the {@link WebsocketAPI#SESSIONS} map.
     *
     * @param session Session associated with the new connection.
     */
    @OnWebSocketConnect
    public void connected(Session session) {
        session.getPolicy().setMaxTextMessageSize(Config.sharedConfig().getApi().getMaxMessageSize());
        session.getPolicy().setMaxBinaryMessageSize(Config.sharedConfig().getApi().getMaxMessageSize());
        SESSIONS.add(session);
        LOGGER.debug("New session {} connected!", session.getRemoteAddress().toString());
    }

    /**
     * Invoked whenever a new connection is closed. Removes the session from the {@link WebsocketAPI#SESSIONS} map.
     *
     * @param session Session associated with the new connection.
     * @param statusCode
     * @param reason
     */
    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        SESSIONS.remove(session);
        LOGGER.debug("Connection of session closed (Code: {}, Reason: {}).", statusCode, reason);
    }

    /**
     * TODO: Handle errors properly.
     *
     * @param session
     * @param error
     */
    @OnWebSocketError
    public void onWebSocketException(Session session, Throwable error) {
        LOGGER.fatal("An unhandled error occurred during message handling: {}", LogHelper.getStackTrace(error));
    }

    /**
     * Handles incoming messages. This method determines the message-type of the message. If
     * that type is known, the message is routed to a pre-registered message handler.
     *
     * @param session Session the message belongs to.
     * @param message String message.
     */
    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        final AnyMessage testMessage = this.reader.toObject(message, AnyMessage.class);
        if (testMessage != null) {
            final MessageType type = testMessage.getMessageType();
            final WebsocketMessageHandler handler = STATELESS_HANDLERS.get(type);
            if (handler != null) {
                handler.handle(session, this.reader.toObject(message, type.getMessageClass()));
            }
        }
    }
}
