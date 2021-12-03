package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

import java.util.List;

/**
 * A {@link SegmentQuery} represents a segment-query message, i.e. a lookup for a segment ID specified.
 */
public class SegmentQuery extends Query {

    /**
     * ID of the {@link MediaSegmentDescriptor} which should be retrieved.
     */
    private final String segmentId;

    private final List<MetadataAccessSpecification> metadataAccessSpec;


    /**
     * Constructor for {@link SegmentQuery} message.
     *
     * @param config The {@link QueryConfig}
     */
    @JsonCreator
    public SegmentQuery(@JsonProperty(value = "segmentId", required = true) String segmentId,
                        @JsonProperty(value = "config", required = false) QueryConfig config,
                        @JsonProperty(value = "metadataAccessSpec", required = false) List<MetadataAccessSpecification> metadataAccessSpec

    ) {
        super(config);
        this.segmentId = segmentId;
        this.metadataAccessSpec = metadataAccessSpec;
    }

    public List<MetadataAccessSpecification> getMetadataAccessSpec() {
        return metadataAccessSpec;
    }

    public String getSegmentId() {
        return segmentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.Q_SEG;
    }
}
