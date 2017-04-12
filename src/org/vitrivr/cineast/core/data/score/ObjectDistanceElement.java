package org.vitrivr.cineast.core.data.score;

import org.vitrivr.cineast.core.data.CorrespondenceFunction;

public class ObjectDistanceElement extends AbstractDistanceElement {
  public ObjectDistanceElement(String id, double distance) {
    super(id, distance);
  }

  public String getObjectId() {
    return this.getId();
  }

  @Override
  public ObjectScoreElement toScore(CorrespondenceFunction f) {
    double score = f.applyAsDouble(this.getDistance());
    return new ObjectScoreElement(this.getObjectId(), score);
  }
}
