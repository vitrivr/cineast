package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

public class NeighboringSegmentQuery extends Query {

    /** ID of the {@link MediaSegmentDescriptor} for which neighbors should be retrieved. */
    private final String segmentId;

    /** Number of neighbors that should be retrieved. */
    private final int count;

    /** */
    @JsonCreator
    public NeighboringSegmentQuery(@JsonProperty(value = "segmentId", required = true) String segmentId,
                                   @JsonProperty(value = "count", required = false) Integer count,
                                   @JsonProperty(value = "config", required = false) QueryConfig config) {
        super(config);
        this.segmentId = segmentId;
        this.count = count == null ? 3 : count;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public int getCount() {
        return count;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.Q_NESEG;
    }
}
