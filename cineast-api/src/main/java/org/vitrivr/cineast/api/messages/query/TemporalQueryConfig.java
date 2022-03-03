package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.vitrivr.cineast.core.config.QueryConfig;

public class TemporalQueryConfig extends QueryConfig {

  /**
   * List of time distances as floats that can be part of this {@link TemporalQuery}.
   */
  public final List<Float> timeDistances;

  /**
   * The max length of the temporal sequences as float that can be part of this {@link TemporalQuery}.
   */
  public final Float maxLength;

  public final boolean computeTemporalObjects;


  public TemporalQueryConfig(@JsonProperty(value = "queryId", required = false) String queryId,
      @JsonProperty(value = "hints", required = false) List<Hints> hints,
      @JsonProperty(value = "timeDistances", required = false) List<Float> timeDistances,
      @JsonProperty(value = "maxLength", required = false) Float maxLength,
      @JsonProperty(value = "computeTemporalObjects", required = false) Boolean computeTemporalObjects
  ) {
    super(queryId, hints);
    this.timeDistances = timeDistances == null ? new ArrayList<>() : timeDistances;
    this.maxLength = maxLength == null ? Float.MAX_VALUE : maxLength;
    this.computeTemporalObjects = computeTemporalObjects == null || computeTemporalObjects;
  }
}
