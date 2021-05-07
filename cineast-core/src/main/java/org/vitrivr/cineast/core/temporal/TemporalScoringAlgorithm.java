package org.vitrivr.cineast.core.temporal;

import java.util.List;
import org.vitrivr.cineast.core.data.TemporalObject;

public interface TemporalScoringAlgorithm {

  public List<TemporalObject> score();

}
