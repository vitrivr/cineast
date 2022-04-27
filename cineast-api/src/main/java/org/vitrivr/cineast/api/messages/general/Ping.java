package org.vitrivr.cineast.api.messages.general;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message object for a ping message.
 */
public record Ping(StatusEnum status, MessageType messageType) implements Message {

  public Ping(StatusEnum status) {
    this(status, MessageType.PING);
  }

}
