package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

/**
 * This object represents a MoreLikeThisQuery message, i.e. a request for a similarity-search.
 */
public class MoreLikeThisQuery extends Query {

  /**
   * ID of the segment that serves as basis for the MLT query.
   */
  private final String segmentId;

  /**
   * List of feature categories that should be considered by the MLT query.
   */
  private final List<String> categories;
  private List<MetadataAccessSpecification> metadataAccessSpec;

  /**
   * Constructor for the SimilarityQuery object.
   *
   * @param segmentId  SegmentId.
   * @param categories List of named feature categories that should be considered when doing More-Like-This.
   * @param config     The {@link ReadableQueryConfig}. May be null!
   */
  @JsonCreator
  public MoreLikeThisQuery(@JsonProperty(value = "segmentId", required = true) String segmentId,
      @JsonProperty(value = "categories", required = true) List<String> categories,
      @JsonProperty(value = "config", required = false) QueryConfig config,
      @JsonProperty(value = "metadataAccessSpec", required = false) List<MetadataAccessSpecification> metadataAccessSpec
  ) {
    super(config);
    this.segmentId = segmentId;
    this.categories = categories;
    this.metadataAccessSpec = metadataAccessSpec;
  }

  public String getSegmentId() {
    return segmentId;
  }

  public List<String> getCategories() {
    return this.categories;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.Q_MLT;
  }

  public List<MetadataAccessSpecification> getMetadataAccessSpec() {
    return metadataAccessSpec;
  }
}


