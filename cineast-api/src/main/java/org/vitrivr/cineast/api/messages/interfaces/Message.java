package org.vitrivr.cineast.api.messages.interfaces;

/**
 * Basic interface of every Message, which can be identified by its MessageType field.
 */
public interface Message {

  /**
   * Returns the type of particular message. Used to determine an appropriate handler in the websocket-context. Used in this way to ensure compatibility with records who call the variable appropriately
   *
   * @return {@link MessageType}
   */
  MessageType messageType();

  default MessageType getMessageType() {
    return messageType();
  }
}
