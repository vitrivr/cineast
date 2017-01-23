package org.vitrivr.cineast.api.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import org.vitrivr.cineast.api.websocket.handlers.QueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.StatusMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.interfaces.WebsocketMessageHandler;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageTypes;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

import spark.Spark;

import java.io.IOException;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


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

    private static Logger LOGGER = LogManager.getLogger();

    /** Store sessions if you want to, for example, broadcast a message to all users.*/
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    /** Maximum size of a text-message. Should be large enough so as to be able to support transmission of image/audio data. */
    private static final int MAX_TEXT_MESSAGE_SIZE = 2048 * 1000;

    /** Named context of the endpoint. Will be appended to the endpoint URL. */
    private static final String CONTEXT = "api";

    /** Version of the protocol used by the endpoint. Will be appended to the endpoint URL. */
    private static final String VERSION = "v1";

    /** List of stateless WebsocketMessageHandler classes for the API. */
    private static final HashMap<MessageTypes, WebsocketMessageHandler> STATELESS_HANDLERS = new HashMap<>();

    /* Register the MessageHandlers for the different messages. */
    static {
        STATELESS_HANDLERS.put(MessageTypes.Q_QUERY, new QueryMessageHandler());
        STATELESS_HANDLERS.put(MessageTypes.PING, new StatusMessageHandler());
    }

    /* */
    private JacksonJsonProvider reader = new JacksonJsonProvider();

    /**
     * Starts the WebSocket API.
     *
     * @param port Port on which the WebSocket endpoint should listen.
     * @param numberOfThreads Maximum number of threads that should be used to handle messages.
     */
    public static void start(int port, int numberOfThreads) {
        if (port > 0 && port < 65535) {
            Spark.port(port);
        } else {
            LOGGER.warn("The specified port {} is not valid. Fallback to default port.", port);
        }
        Spark.threadPool(numberOfThreads, 2, 30000);
        Spark.webSocket(String.format("/%s/%s", CONTEXT, VERSION), WebsocketAPI.class);
        Spark.init();
        Spark.awaitInitialization();
    }

    /**
     * Can be used to register new WebsocketMessageHandlers at runtime.
     *
     * @param type MessageType for which a new handler should be regiestered.
     * @param handler Instance of WebsocketMessageHandler.
     */
    private static void registerHandlerForMessageType(MessageTypes type, WebsocketMessageHandler handler) {
        if (handler.isStateless()) {
            STATELESS_HANDLERS.put(type, handler);
        }
    }

    /**
     * Invoked whenever a new connection is established. Configures the session and
     * stashes it in the SESSIONS map.
     *
     * @param session Session associated with the new connection.
     */
    @OnWebSocketConnect
    public void connected(Session session) {
        session.getPolicy().setMaxTextMessageSize(MAX_TEXT_MESSAGE_SIZE);
        sessions.add(session);
        LOGGER.debug("New session {} connected!");
    }

    /**
     * Invoked whenever a new connection is closed. Removes the session from
     * the SESSIONS map.
     *
     * @param session
     * @param statusCode
     * @param reason
     */
    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
        LOGGER.debug("Connection of session closed (Code: {}, Reason: {}).", statusCode, reason);
    }


    /**
     * TODO: Handle errors properly.
     *
     * @param session
     * @param error
     */
    @OnWebSocketError
    public void onWebSocketException(Session session, Throwable error)
    {
        LOGGER.fatal("An unhandled error occurred during message handling.", LogHelper.getStackTrace(error));
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
        AnyMessage testMessage = reader.toObject(message, AnyMessage.class);
        if (testMessage != null) {
            MessageTypes type = testMessage.getMessagetype();
            WebsocketMessageHandler handler = STATELESS_HANDLERS.get(type);
            if (handler != null) {
                handler.handle(session, reader.toObject(message, type.getMessageClass()));
            }
        }
    }
}
