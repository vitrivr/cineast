package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.websocket.WebsocketAPI;

/**
 * Message type for a non-particular message that implements the message interface. Is used in the {@link WebsocketAPI} to find out the {@link MessageType} of arriving messages
 */
public record AnyMessage(@JsonProperty(required = true) MessageType messageType) implements Message {

}
