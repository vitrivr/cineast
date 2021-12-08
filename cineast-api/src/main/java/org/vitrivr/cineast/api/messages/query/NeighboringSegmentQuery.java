package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

import java.util.List;

/**
 * A {@link NeighboringSegmentQuery} represents a query for neighbors of a given segment ID.
 */
public class NeighboringSegmentQuery extends Query {

    /**
     * ID of the {@link MediaSegmentDescriptor} for which neighbors should be retrieved.
     */
    private final String segmentId;

    /**
     * Number of neighbors that should be retrieved.
     */
    private final int count;

    private final List<MetadataAccessSpecification> metadataAccessSpec;

    /**
     * Constructor for the NeighboringSegmentQuery object.
     *
     * @param segmentId ID of the {@link MediaSegmentDescriptor}.
     * @param count     Number of neighbors to be retrieved.
     * @param config    The {@link QueryConfig}. May be null!
     */
    @JsonCreator
    public NeighboringSegmentQuery(@JsonProperty(value = "segmentId", required = true) String segmentId,
                                   @JsonProperty(value = "count", required = false) Integer count,
                                   @JsonProperty(value = "config", required = false) QueryConfig config,
                                   @JsonProperty(value = "metadataAccessSpec", required = false) List<MetadataAccessSpecification> metadataAccessSpec
    ) {
        super(config);
        this.segmentId = segmentId;
        this.count = count == null ? 3 : count;
        this.metadataAccessSpec = metadataAccessSpec;
    }

    public List<MetadataAccessSpecification> getMetadataAccessSpec() {
        return metadataAccessSpec;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public int getCount() {
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.Q_NESEG;
    }

}
