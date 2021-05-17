package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message object for an error message.
 *
 * @author rgasser
 * @created 19.01.17
 */
public class Error implements Message {

  /**
   * Message content of the error.
   */
  private String message;

  /**
   * Timestamp when the error was recorded.
   */
  private long timestamp;

  /**
   * Constructor for the Error object. Saves the timestamp of the creation of this error message.
   *
   * @param message Error message.
   */
  @JsonCreator
  public Error(String message) {
    this.message = message;
    this.timestamp = System.currentTimeMillis();
  }

  @JsonProperty
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @JsonProperty
  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return null;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
