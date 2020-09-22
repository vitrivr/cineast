package org.vitrivr.cineast.core.data.score;

public class ObjectScoreElement extends AbstractScoreElement {
  public ObjectScoreElement(String objectId, double score) {
    super(objectId, score);
  }

  public String getObjectId() {
    return this.getId();
  }

  @Override
  public ObjectScoreElement withScore(double newScore) {
    return new ObjectScoreElement(this.getObjectId(), newScore);
  }
}
