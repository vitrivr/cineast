package org.vitrivr.cineast.api.messages.interfaces;

/**
 *
 * Basic interface of every Message, which can be identified by its MessageType field.
 *
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public interface Message {
    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    public MessageType getMessageType();
}
