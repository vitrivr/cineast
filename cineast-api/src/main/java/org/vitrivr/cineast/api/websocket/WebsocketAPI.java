package org.vitrivr.cineast.api.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.websocket.handlers.MetadataLookupMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.StatusMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.interfaces.WebsocketMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.MoreLikeThisQueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.NeighbouringQueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.SegmentQueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.SimilarityQueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.TemporalQueryMessageHandler;
import org.vitrivr.cineast.api.websocket.handlers.queries.TemporalQueryMessageHandlerV2;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.standalone.config.Config;


/**
 * This class establishes a WebSocket endpoint listening on the specified port. Incoming messages are expected to come in a JSON format and to have an application specific POJO representation. This class maps those messages to such a POJO and routes them towards a WebsocketMessageHandler provided that such a handler has been registered.
 *
 * @see WebsocketMessageHandler
 * @see Message
 */
@WebSocket
public class WebsocketAPI {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Store SESSIONS if you want to, for example, broadcast a message to all users.
   */
  private static final Queue<Session> SESSIONS = new ConcurrentLinkedQueue<>();

  /**
   * A cached ThreadPoolExecutor used to execute tasks submitted and handled by the WebSocket API
   */
  private static final ExecutorService EXECUTORS = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

  /**
   * List of stateless {@link WebsocketMessageHandler} classes for the API.
   */
  private static final HashMap<MessageType, WebsocketMessageHandler<?>> STATELESS_HANDLERS = new HashMap<>();

  static {
    STATELESS_HANDLERS.put(MessageType.Q_SIM, new SimilarityQueryMessageHandler(APIEndpoint.retrievalLogic));
    STATELESS_HANDLERS.put(MessageType.Q_TEMPORAL, new TemporalQueryMessageHandler(APIEndpoint.retrievalLogic));
    STATELESS_HANDLERS.put(MessageType.Q_TEMPORALV2, new TemporalQueryMessageHandlerV2(APIEndpoint.retrievalLogic));
    STATELESS_HANDLERS.put(MessageType.Q_MLT, new MoreLikeThisQueryMessageHandler(APIEndpoint.retrievalLogic));
    STATELESS_HANDLERS.put(MessageType.Q_NESEG, new NeighbouringQueryMessageHandler());
    STATELESS_HANDLERS.put(MessageType.Q_SEG, new SegmentQueryMessageHandler());
    STATELESS_HANDLERS.put(MessageType.PING, new StatusMessageHandler());
    STATELESS_HANDLERS.put(MessageType.M_LOOKUP, new MetadataLookupMessageHandler());
  }

  /* */
  private JacksonJsonProvider reader = new JacksonJsonProvider();

  /**
   * Shuts down this WebsocketAPIs ThreadPoolExecutor.
   */
  public void shutdown() {
    EXECUTORS.shutdown();
    try {
      EXECUTORS.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    EXECUTORS.shutdownNow();
  }

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
   */
  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason) {
    SESSIONS.remove(session);
    LOGGER.debug("Connection of session closed (Code: {}, Reason: {}).", statusCode, reason);
  }

  /*
   * TODO: Handle errors properly.
   */
  @OnWebSocketError
  public void onWebSocketException(Session session, Throwable error) {
    LOGGER.fatal("An unhandled error occurred during message handling: {}", LogHelper.getStackTrace(error));
  }

  /**
   * Handles incoming messages. This method determines the message-type of the message. If that type is known, the message is routed to a pre-registered message handler.
   *
   * @param session Session the message belongs to.
   * @param message String message.
   */
  @OnWebSocketMessage
  @SuppressWarnings("unchecked")
  public void message(Session session, String message) throws IOException {
    final AnyMessage testMessage = this.reader.toObject(message, AnyMessage.class);
    if (testMessage != null) {
      final MessageType type = testMessage.getMessageType();
      final WebsocketMessageHandler handler = STATELESS_HANDLERS.get(type);
      if (handler != null) {
        EXECUTORS.execute(() -> handler.handle(session, this.reader.toObject(message, type.getMessageClass())));
      }
    }
  }
}
