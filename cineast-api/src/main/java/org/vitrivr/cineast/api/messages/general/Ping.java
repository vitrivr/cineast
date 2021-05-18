package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message object for an ping message.
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

  @JsonProperty
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
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
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
