package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message object for an error message.
 *
 * @author rgasser
 * @version 1.0
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

  /**
   * Setter for timestamp.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Setter for message.
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Getter for message.
   *
   * @return String
   */
  @JsonProperty
  public String getMessage() {
    return message;
  }

  /**
   * Getter for timestamp.
   *
   * @return long
   */
  @JsonProperty
  public long getTimestamp() {
    return timestamp;
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
    return "Error{" +
        "message='" + message + '\'' +
        ", timestamp=" + timestamp +
        '}';
  }
}
