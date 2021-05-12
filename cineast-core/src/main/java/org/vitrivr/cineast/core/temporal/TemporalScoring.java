package org.vitrivr.cineast.core.temporal;

import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.temporal.sequential.SequentialTemporalScoringAlgorithm;
import org.vitrivr.cineast.core.temporal.timedistance.TimeDistanceTemporalScoringAlgorithm;

public class TemporalScoring {

  private final Map<String, MediaSegmentDescriptor> segmentMap;
  private final List<List<StringDoublePair>> containerResults;
  private final List<Float> timeDistances;
  private final Float maxLength;

  public TemporalScoring(Map<String, MediaSegmentDescriptor> segmentMap, List<List<StringDoublePair>> containerResults, List<Float> timeDistances, Float maxLength) {
    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
    this.timeDistances = timeDistances;
    this.maxLength = maxLength;
  }

  public List<TemporalObject> score() {
    TemporalScoringAlgorithm temporalScoringAlgorithm;
    if (this.timeDistances.size() > 0) {
      temporalScoringAlgorithm = new TimeDistanceTemporalScoringAlgorithm(this.segmentMap, this.containerResults, this.timeDistances, this.maxLength);
    } else {
      temporalScoringAlgorithm = new SequentialTemporalScoringAlgorithm(this.segmentMap, this.containerResults, this.maxLength);
    }
    return temporalScoringAlgorithm.score();
  }
}
