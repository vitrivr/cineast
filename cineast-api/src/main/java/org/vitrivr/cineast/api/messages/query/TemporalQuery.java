package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import kotlin.collections.ArrayDeque;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;
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
   * List of time distances as floats that can be part of this {@link TemporalQuery}.
   */
  private final List<Float> timeDistances;

  /**
   * The max length of the temporal sequences as float that can be part of this {@link TemporalQuery}.
   */
  private final Float maxLength;

  private final List<MetadataAccessSpecification> metadataAccessSpec;

  @JsonCreator
  public TemporalQuery(
      @JsonProperty(value = "queries", required = true) List<StagedSimilarityQuery> queries,
      @JsonProperty(value = "config", required = false) QueryConfig config,
      @JsonProperty(value = "timeDistances", required = false) List<Float> timeDistances,
      @JsonProperty(value = "maxLength", required = false) Float maxLength,
      @JsonProperty(value = "metadataAccessSpec", required = false) List<MetadataAccessSpecification> metadataAccessSpec
  ) {
    super(config);
    this.queries = queries;
    this.timeDistances = timeDistances == null ? new ArrayList<>() : timeDistances;
    this.maxLength = maxLength == null ? Float.MAX_VALUE : maxLength;
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
    return timeDistances;
  }

  /**
   * Getter for maxLength.
   *
   * @return Float
   */
  public Float getMaxLength() {
    return maxLength;
  }

  public List<MetadataAccessSpecification> getMetadataAccessSpec() {
    return metadataAccessSpec;
  }

  /**
   * Returns the type of particular message. Expressed as MessageTypes enum.
   *
   * @return {@link MessageType}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.Q_TEMPORAL;
  }
}
