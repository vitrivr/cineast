package org.vitrivr.cineast.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TemporalObject {

  private final List<String> segments;
  private final String objectId;
  private final double score;

  @JsonCreator
  public TemporalObject(@JsonProperty("segments") List<String> segments, @JsonProperty("objectId") String objectId, @JsonProperty("score") double score) {
    this.segments = segments;
    this.objectId = objectId;
    this.score = score;
  }

  public String getObjectId() {
    return objectId;
  }

  public double getScore() {
    return score;
  }

  public List<String> getSegments() {
    return segments;
  }

  public static final Comparator<TemporalObject> COMPARATOR = Comparator.comparingDouble(TemporalObject::getScore);

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
