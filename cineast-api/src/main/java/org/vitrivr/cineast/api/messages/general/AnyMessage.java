package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message type for a non-particular message that implements the message interface.
 *
 * @author rgasser
 * @created 19.01.17
 */
public class AnyMessage implements Message {

  /**
   * {@link MessageType} of the message.
   */
  private MessageType messageType;

  @Override
  @JsonProperty
  public MessageType getMessageType() {
    return this.messageType;
  }

  public void setMessagetype(MessageType messageType) {
    this.messageType = messageType;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
