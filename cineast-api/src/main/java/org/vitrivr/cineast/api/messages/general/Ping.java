package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message object for a ping message.
 */
public record Ping(StatusEnum status, @JsonProperty(required = true) MessageType messageType) implements Message {

  public Ping(StatusEnum status) {
    this(status, MessageType.PING);
  }

}
