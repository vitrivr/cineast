package org.vitrivr.cineast.api.messages.general;

/**
 * Message object for an error message.
 */
public record Error(String message, Long timestamp) {

  public Error(String message) {
    this(message, System.currentTimeMillis());
  }

}
