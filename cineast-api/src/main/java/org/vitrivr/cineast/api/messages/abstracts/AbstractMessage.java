package org.vitrivr.cineast.api.messages.abstracts;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Every message class should extend this class, which provides a sensible toString() implementation for logging
 */
public abstract class AbstractMessage implements Message {

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }

  /**
   * Message Types which are not used in a WS context can simply have null as message type
   */
  @Override
  public MessageType getMessageType() {
    return null;
  }
}
