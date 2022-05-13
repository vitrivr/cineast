package org.vitrivr.cineast.api.websocket.handlers;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.general.Ping;
import org.vitrivr.cineast.api.messages.general.StatusEnum;
import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;

/**
 * This class extends the {@link StatelessWebsocketMessageHandler} abstract class and handles messages of type {@link Ping}.
 */
public class StatusMessageHandler extends StatelessWebsocketMessageHandler<Ping> {

  /**
   * Invoked when a Message of type Ping arrives and requires handling. Sets the Ping status of the message to OK and writes it back on the WebSocket.
   *
   * @param session WebSocketSession for which the message arrived.
   * @param message Message of type a that needs to be handled.
   */
  @Override
  public void handle(Session session, Ping message) {
    this.write(session, new Ping(StatusEnum.OK));
  }
}
