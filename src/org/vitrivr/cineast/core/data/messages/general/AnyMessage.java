package org.vitrivr.cineast.core.data.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageTypes;

/**
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public class AnyMessage implements Message {
    private MessageTypes messagetype;

    @Override
    @JsonProperty
    public MessageTypes getMessagetype() {
        return this.messagetype;
    }
    public void setMessagetype(MessageTypes messagetype) {
        this.messagetype = messagetype;
    }
}
