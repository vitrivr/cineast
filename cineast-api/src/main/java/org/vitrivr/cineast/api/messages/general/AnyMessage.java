package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message type for a non-particular message that implements the message interface.
 *
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public class AnyMessage implements Message {

  /**
   * {@link MessageType} of the message.
   */
  private MessageType messageType;

  /**
   * Setter for message type.
   */
  public void setMessagetype(MessageType messageType) {
    this.messageType = messageType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @JsonProperty
  public MessageType getMessageType() {
    return this.messageType;
  }

  @Override
  public String toString() {
    return "AnyMessage{" +
        "messageType=" + messageType +
        '}';
  }
}
