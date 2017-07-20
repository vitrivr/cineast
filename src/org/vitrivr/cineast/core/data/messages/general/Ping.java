package org.vitrivr.cineast.core.data.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

/**
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public class Ping implements Message {
    public enum StatusEnum {
        UNKNOWN,OK,ERROR
    }

    /** */
    private StatusEnum status = StatusEnum.UNKNOWN;

    @JsonProperty
    public StatusEnum getStatus() {
        return status;
    }
    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.PING;
    }
}
