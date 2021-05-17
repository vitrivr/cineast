package org.vitrivr.cineast.api.messages.interfaces;

/**
 * Basic interface of every Message, which can be identified by its MessageType field.
 *
 * @author rgasser
 * @created 12.01.17
 */
public interface Message {

  /**
   * Returns the type of particular message. Expressed as MessageTypes enum.
   *
   * @return {@link MessageType}
   */
  public MessageType getMessageType();
}
