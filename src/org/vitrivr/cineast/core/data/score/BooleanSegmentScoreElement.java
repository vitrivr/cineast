package org.vitrivr.cineast.core.data.score;

public class BooleanSegmentScoreElement implements ScoreElement {

  private final String id;

  public BooleanSegmentScoreElement(String segmentId){
    this.id = segmentId;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public double getScore() {
    return 1d;
  }

  @Override
  public SegmentScoreElement withScore(double newScore) {
    return new SegmentScoreElement(this.id, newScore);
  }
}
