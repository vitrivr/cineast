package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message object for an ping message.
 *
 * @author rgasser
 * @created 19.01.17
 */
public class Ping implements Message {

  /**
   * Enum of the Ping status.
   */
  public enum StatusEnum {
    UNKNOWN, OK, ERROR
  }

  /**
   * Status of the Ping.
   */
  private StatusEnum status = StatusEnum.UNKNOWN;

  /**
   * Setter for status.
   */
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Getter for status.
   *
   * @return {@link StatusEnum}
   */
  @JsonProperty
  public StatusEnum getStatus() {
    return status;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.PING;
  }

  @Override
  public String toString() {
    return "Ping{" +
        "status=" + status +
        '}';
  }
}
