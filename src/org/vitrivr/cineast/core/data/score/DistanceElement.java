package org.vitrivr.cineast.core.data.score;

import org.vitrivr.cineast.core.data.CorrespondenceFunction;

public interface DistanceElement {
  String getId();

  double getDistance();

  ScoreElement toScore(CorrespondenceFunction f);
}
