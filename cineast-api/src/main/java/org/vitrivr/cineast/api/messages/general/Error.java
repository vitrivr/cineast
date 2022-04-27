package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message object for an error message.
 */
public class Error extends AbstractMessage {

  /**
   * Message content of the error.
   */
  private final String message;

  /**
   * Timestamp when the error was recorded.
   */
  private final long timestamp;

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

  @JsonProperty
  public long getTimestamp() {
    return timestamp;
  }

}
