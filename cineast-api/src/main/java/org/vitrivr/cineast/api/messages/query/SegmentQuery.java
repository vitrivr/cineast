package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

/**
 * A {@link SegmentQuery} represents a segment-query message, i.e. a lookup for a segment ID specified.
 *
 * @author rgasser
 * @created 28.12.18
 */
public class SegmentQuery extends Query {

  /**
   * ID of the {@link MediaSegmentDescriptor} which should be retrieved.
   */
  private final String segmentId;

  /**
   * Constructor for {@link SegmentQuery} message.
   *
   * @param config The {@link QueryConfig}
   */
  @JsonCreator
  public SegmentQuery(@JsonProperty(value = "segmentId", required = true) String segmentId,
      @JsonProperty(value = "config", required = false) QueryConfig config) {
    super(config);
    this.segmentId = segmentId;
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
