package org.vitrivr.cineast.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;

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

  @Override
  public String toString() {
    return "TemporalObject(" + objectId + ", " + score + ")";
  }

  public static final Comparator<TemporalObject> COMPARATOR = new Comparator<TemporalObject>() {
    public int compare(TemporalObject o1, TemporalObject o2) {
      return Double.compare(o2.getScore(), o1.getScore());
    }
  };

}
