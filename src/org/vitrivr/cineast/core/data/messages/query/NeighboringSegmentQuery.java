package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.Pair;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Returns a list of segment ID's that can be used for lookup to satisfy this query.
     *
     * @return List of segment IDs that satisfy this {@link NeighboringSegmentQuery}.
     */
    public List<String> getSegmentIds() {
        final Pair<String,Long> decomposed = MediaType.parsesSegmentId(this.segmentId);
        final long segmentNumber = decomposed.getRight();
        final long start = Math.max(0, segmentNumber - this.count);
        final long end = Math.max(0, segmentNumber + this.count);
        final ArrayList<String> list = new ArrayList<>((int)(end-start));
        for (long i=start; i<=end; i++) {
            if (i != segmentNumber) {
                list.add(MediaType.generateSegmentId(decomposed.getLeft(), i));
            }
        }
        return list;
    }


    @Override
    public MessageType getMessageType() {
        return null;
    }
}
