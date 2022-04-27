package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message object for a ping message.
 */
public class Ping extends AbstractMessage {

  /**
   * Status of the Ping.
   */
  private final StatusEnum status;

  public Ping(StatusEnum status) {
    this.status = status;
  }

  @JsonProperty
  public StatusEnum getStatus() {
    return status;
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.PING;
  }

  /**
   * Enum of the Ping status.
   */
  public enum StatusEnum {
    UNKNOWN, OK, ERROR
  }
}
