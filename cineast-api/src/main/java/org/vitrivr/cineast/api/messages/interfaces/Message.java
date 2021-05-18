package org.vitrivr.cineast.api.messages.interfaces;

/**
 * Basic interface of every Message, which can be identified by its MessageType field.
 */
public interface Message {

  /**
   * Returns the type of particular message. Expressed as MessageTypes enum.
   *
   * @return {@link MessageType}
   */
  public MessageType getMessageType();
}
