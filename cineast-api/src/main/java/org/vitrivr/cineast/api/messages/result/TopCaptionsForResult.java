package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

public class TopCaptionsForResult implements Message {

    public final String queryId;
    public final Map<String, Integer> captions;

    @JsonCreator
    public TopCaptionsForResult(String queryId, Map<String, Integer> captions) {
        this.queryId = queryId;
        this.captions = captions;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.QR_TOPCAPTIONS;
    }
}
