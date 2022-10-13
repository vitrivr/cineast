package org.vitrivr.cineast.api.messages.session;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

public record SessionMessage(String message, MessageType messageType) implements Message {

  public SessionMessage(String message) {
    this(message, MessageType.SESSION_INFO);
  }

}
