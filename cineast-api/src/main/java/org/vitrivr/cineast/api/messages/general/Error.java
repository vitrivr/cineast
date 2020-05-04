package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public class Error implements Message {

    private String message;
    private long timestamp;

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
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
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
