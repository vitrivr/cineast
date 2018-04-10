package org.vitrivr.cineast.core.data.distance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;

public class SegmentDistanceElement extends AbstractDistanceElement {

  @JsonCreator
  public SegmentDistanceElement(
      @JsonProperty(value = "segmentId", required = true) String id,
      @JsonProperty(value = "distance", required = true) double distance) {
    super(id, distance);
  }

  public String getSegmentId() {
    return this.getId();
  }

  @Override
  public SegmentScoreElement toScore(CorrespondenceFunction f) {
    double score = f.applyAsDouble(this.getDistance());
    return new SegmentScoreElement(this.getSegmentId(), score);
  }
}
