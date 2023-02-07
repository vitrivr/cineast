package org.vitrivr.cineast.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record TemporalObject(List<String> segments, String objectId, double score) {

  public static final Comparator<TemporalObject> COMPARATOR = Comparator.comparingDouble(TemporalObject::score);

  @JsonCreator
  public TemporalObject(@JsonProperty("segments") List<String> segments, @JsonProperty("objectId") String objectId, @JsonProperty("score") double score) {
    this.segments = segments;
    this.objectId = objectId;
    this.score = score;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
