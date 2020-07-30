package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.pose.PoseKeypointsResultContainer;

import java.util.List;

public class SkelLookupResult extends AbstractQueryResultMessage<PoseKeypointsResultContainer> {

    @JsonCreator
    public SkelLookupResult(String queryId, List<PoseKeypointsResultContainer> content) {
        super(queryId, PoseKeypointsResultContainer.class, content);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.QR_SKEL;
    }
}
