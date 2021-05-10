package org.vitrivr.cineast.api.websocket.handlers.abstracts;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.websocket.handlers.interfaces.WebsocketMessageHandler;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.json.JsonWriter;

/**
 * This abstract class implements the WebsocketMessageHandler interface and provides basic functionality like a convenience method to write information back to the underlying WebSocket stream.
 *
 * @author rgasser
 * @version 1.0
 * @created 22.01.17
 */
public abstract class AbstractWebsocketMessageHandler<A> implements WebsocketMessageHandler<A> {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * JsonWriter used to serialize resulting objects to a JSON representation.
   */
  private final JsonWriter writer = new JacksonJsonProvider();

  /**
   * Writes a message back to the stream.
   */
  protected final CompletableFuture<Void> write(Session session, Message message) {

    StopWatch watch = StopWatch.createStarted();
    String json = this.writer.toJson(message);
    if (message.getMessageType() != MessageType.PING) {
      LOGGER.trace("Serialization for {} in {} ms", message.getMessageType(), watch.getTime(TimeUnit.MILLISECONDS));
    }
    String callbackName = Thread.currentThread().getName();
    CompletableFuture<Void> future = new CompletableFuture<>();
    session.getRemote().sendString(json, new WriteCallback() {
      @Override
      public void writeFailed(Throwable x) {
        future.completeExceptionally(x);
        LOGGER.fatal("Failed to write {} message to WebSocket stream!", message.getMessageType());
      }

      @Override
      public void writeSuccess() {
        future.complete(null);
        if (message.getMessageType() == MessageType.PING) {
          return;
        }
        watch.stop();
        LOGGER.trace("{}: Successfully wrote message {} in {} ms", callbackName, message.getMessageType(), watch.getTime(TimeUnit.MILLISECONDS));
      }
    });
    return future;
  }
}
