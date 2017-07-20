package org.vitrivr.cineast.api.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import org.vitrivr.cineast.api.websocket.handlers.MetadataLookupMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.MoreLikeThisQueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.SimilarityQueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.StatusMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.interfaces.WebsocketMessageHandler;
import org.vitrivr.cineast.core.config.APIConfig;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

import spark.Spark;

import java.io.IOException;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


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

    /** Named context of the endpoint. Will be appended to the endpoint URL. */
    private static final String CONTEXT = "api";

    /** Version of the protocol used by the endpoint. Will be appended to the endpoint URL. */
    private static final String VERSION = "v1";

    /** List of stateless WebsocketMessageHandler classes for the API. */
    private static final HashMap<MessageType, WebsocketMessageHandler<?>> STATELESS_HANDLERS = new HashMap<>();

    /** Flag that indicates whether the WebSocket API is running. */
    private static AtomicBoolean RUNNING = new AtomicBoolean(false);

    /* Register the MessageHandlers for the different messages. */
    static {
        STATELESS_HANDLERS.put(MessageType.Q_SIM, new SimilarityQueryMessageHandler());
        STATELESS_HANDLERS.put(MessageType.Q_MLT, new MoreLikeThisQueryMessageHandler());
        STATELESS_HANDLERS.put(MessageType.PING, new StatusMessageHandler());
        STATELESS_HANDLERS.put(MessageType.M_LOOKUP, new MetadataLookupMessageHandler());
    }

    /* */
    private JacksonJsonProvider reader = new JacksonJsonProvider();

    /**
     * Starts the WebSocket API.
     *
     * @param config APIConfig object.
     */
    public static void start(APIConfig config) {
        if (config.getHttpPort() > 0 && config.getHttpPort() < 65535) {
            Spark.port(config.getHttpPort());
        } else {
            LOGGER.warn("The specified port {} is not valid. Fallback to default port.", config.getHttpPort());
        }
        Spark.threadPool(config.getThreadPoolSize(), 2, 30000);
        if (config.isSecure()) {
            Spark.secure(config.getKeystore(), config.getKeystorePassword(), null, null);
        }

        Spark.webSocket(String.format("/%s/%s", CONTEXT, VERSION), WebsocketAPI.class);
        Spark.init();
        Spark.awaitInitialization();
        RUNNING.set(true);
    }

    /**
     * Stops the WebSocket API.
     */
    public static void stop() {
        Spark.stop();
        RUNNING.set(false);
    }

    /**
     * Indicates whether or not the WebSocket API is already running.
     *
     * @return true if WebSocket API is running, false otherwise.
     */
    public static boolean isRunning() {
        return RUNNING.get();
    }

    /**
     * Can be used to register new WebsocketMessageHandlers at runtime.
     *
     * @param type MessageType for which a new handler should be regiestered.
     * @param handler Instance of WebsocketMessageHandler.
     */
    private static void registerHandlerForMessageType(MessageType type, WebsocketMessageHandler<?> handler) {
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
        session.getPolicy().setMaxTextMessageSize(Config.sharedConfig().getApi().getMaxMessageSize());
        session.getPolicy().setMaxBinaryMessageSize(Config.sharedConfig().getApi().getMaxMessageSize());
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
        AnyMessage testMessage = reader.toObject(message, AnyMessage.class);
        if (testMessage != null) {
            MessageType type = testMessage.getMessageType();
            WebsocketMessageHandler handler = STATELESS_HANDLERS.get(type);
            if (handler != null) {
                handler.handle(session, reader.toObject(message, type.getMessageClass()));
            }
        }
    }
}
