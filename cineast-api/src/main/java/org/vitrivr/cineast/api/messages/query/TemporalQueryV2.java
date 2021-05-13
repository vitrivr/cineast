package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

/**
 * This object represents a temporal-query message of temporal query version 2, i.e. a request for a
 * temporally staged similarity-search.
 */
public class TemporalQueryV2 extends Query {

  /**
   * List of {@link StagedSimilarityQuery}s that are part of this {@link TemporalQueryV2}.
   */
  private final List<StagedSimilarityQuery> queries;

  /**
   * List of time distances as floats that can be part of this {@link TemporalQueryV2}.
   */
  private final List<Float> timeDistances;

  /**
   * The max length of the temporal sequences as float that can be part of this {@link
   * TemporalQueryV2}.
   */
  private final Float maxLength;

  /**
   * Constructor for the TemporalQueryV2 object.
   *
   * @param queries       List of {@link StagedSimilarityQuery}s.
   * @param config        The {@link ReadableQueryConfig}. May be null!
   * @param timeDistances List of {@link Float}. May be null!
   * @param maxLength     A {@link Float}. May be null!
   */
  @JsonCreator
  public TemporalQueryV2(@JsonProperty(value = "queries", required = true) List<StagedSimilarityQuery> queries, @JsonProperty(value = "config", required = false) QueryConfig config, @JsonProperty(value = "timeDistances", required = false) List<Float> timeDistances, @JsonProperty(value = "maxLength", required = false) Float maxLength) {
    super(config);
    this.queries = queries;
    this.timeDistances = timeDistances;
    this.maxLength = maxLength;
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

  /**
   * Returns the type of particular message. Expressed as MessageTypes enum.
   *
   * @return {@link MessageType}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.Q_TEMPORALV2;
  }
}
