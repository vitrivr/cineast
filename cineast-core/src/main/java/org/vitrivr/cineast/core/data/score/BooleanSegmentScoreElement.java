package org.vitrivr.cineast.core.data.score;

public class BooleanSegmentScoreElement implements ScoreElement {

  private final String id;
  private final Double weight;

  public BooleanSegmentScoreElement(String segmentId){
    this.id = segmentId;
    this.weight = 1d;
  }
  public BooleanSegmentScoreElement(String segmentId, Double weight){
    this.id = segmentId;
    this.weight = weight;
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
