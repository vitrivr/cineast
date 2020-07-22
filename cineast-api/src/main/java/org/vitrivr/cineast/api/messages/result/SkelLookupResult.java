package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.pose.PoseKeypoints;

import java.util.List;

public class SkelLookupResult extends AbstractQueryResultMessage<PoseKeypoints> {

    @JsonCreator
    public SkelLookupResult(String queryId, List<PoseKeypoints> content) {
        super(queryId, PoseKeypoints.class, content);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.QR_SKEL;
    }
}
