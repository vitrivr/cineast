package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

/**
 * This object represents a temporal-query message of temporal query version 2, i.e. a request for a temporally staged similarity-search.
 */
public class TemporalQuery extends Query {

  /**
   * List of {@link StagedSimilarityQuery}s that are part of this {@link TemporalQuery}.
   */
  private final List<StagedSimilarityQuery> queries;

  /**
   * Provide an empty list to fetch no metadata at all. If the field is not filled (i.e. null), all metadata is provided for backwards-compatibility
   */
  private final List<MetadataAccessSpecification> metadataAccessSpec;

  @JsonCreator
  public TemporalQuery(@JsonProperty(value = "queries", required = true) List<StagedSimilarityQuery> queries, @JsonProperty(value = "config", required = false) TemporalQueryConfig config, @JsonProperty(value = "metadataAccessSpec", required = false) List<MetadataAccessSpecification> metadataAccessSpec) {
    super(config);
    this.queries = queries;
    this.metadataAccessSpec = metadataAccessSpec;
  }

  /**
   * Getter for queries.
   *
   * @return {@link StagedSimilarityQuery}
   */
  public List<StagedSimilarityQuery> getQueries() {
    return queries;
  }

  /**
   * Getter for timeDistances.
   *
   * @return List<Float>
   */
  public List<Float> getTimeDistances() {
    return getTemporalQueryConfig().timeDistances;
  }

  /**
   * Getter for maxLength.
   *
   * @return Float
   */
  public Float getMaxLength() {
    return getTemporalQueryConfig().maxLength;
  }

  public TemporalQueryConfig getTemporalQueryConfig() {
    return (TemporalQueryConfig) this.config;
  }

  public List<MetadataAccessSpecification> getMetadataAccessSpec() {
    return metadataAccessSpec;
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.Q_TEMPORAL;
  }
}
