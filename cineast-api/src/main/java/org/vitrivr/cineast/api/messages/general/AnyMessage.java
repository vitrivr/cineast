package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.websocket.WebsocketAPI;

/**
 * Message type for a non-particular message that implements the message interface. Is used in the {@link WebsocketAPI} to find out the {@link MessageType} of arriving messages
 */
public class AnyMessage extends AbstractMessage {

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

}
