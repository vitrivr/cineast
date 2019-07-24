package org.vitrivr.cineast.core.data.score;

public class SegmentScoreElement extends AbstractScoreElement {
  public SegmentScoreElement(String segmentId, double score) {
    super(segmentId, score);
  }

  public String getSegmentId() {
    return this.getId();
  }

  @Override
  public SegmentScoreElement withScore(double newScore) {
    return new SegmentScoreElement(this.getSegmentId(), newScore);
  }
}
