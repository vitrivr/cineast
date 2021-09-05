package org.vitrivr.cineast.core.data.score;

public class BooleanSegmentScoreElement implements ScoreElement {

  private final String id;
  private final Double weight;
  private final Double factor;

  public BooleanSegmentScoreElement(String segmentId){
    this.id = segmentId;
    this.weight = 1d;
    this.factor = 1d;
  }
// For the extenden Boolean RetrieverModel
  public BooleanSegmentScoreElement(String segmentId, Double weight,Double factor){
    this.id = segmentId;
    this.weight = weight;
    this.factor = factor;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public double getScore() {
    return weight*factor;
  }

  @Override
  public SegmentScoreElement withScore(double newScore) {
    return new SegmentScoreElement(this.id, newScore);
  }
}
