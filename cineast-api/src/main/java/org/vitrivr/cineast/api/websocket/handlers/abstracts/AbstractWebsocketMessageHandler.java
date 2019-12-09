package org.vitrivr.cineast.api.websocket.handlers.abstracts;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.vitrivr.cineast.api.websocket.handlers.interfaces.WebsocketMessageHandler;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
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
  private JsonWriter writer = new JacksonJsonProvider();

  /**
   * Writes a message back to the stream.
   */
  protected final void write(Session session, Message message) {
    session.getRemote().sendString(this.writer.toJson(message), new WriteCallback() {
      @Override
      public void writeFailed(Throwable x) {
        LOGGER.fatal("Failed to write {} message to WebSocket stream!", message.getMessageType());
      }

      @Override
      public void writeSuccess() {
        Level lvl = Level.DEBUG;
        if (message.getMessageType() == MessageType.PING) {
          return;
        }
        LOGGER.log(lvl, "Successfully wrote {} message to WebSocket stream!", writer.toJson(message));
      }
    });
  }
}
