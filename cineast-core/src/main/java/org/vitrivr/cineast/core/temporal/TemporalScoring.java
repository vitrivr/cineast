package org.vitrivr.cineast.core.temporal;

import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.temporal.sequential.SequentialTemporalScoringAlgorithm;
import org.vitrivr.cineast.core.temporal.timedistance.TimeDistanceTemporalScoringAlgorithm;

/**
 * Class to evaluate temporal sequences either with a time distance temporal algorithm or with a sequential temporal algorithm, depending on the provided information.
 *
 * @author vGsteiger
 * @created 06.05.2021
 */
public class TemporalScoring {

  private final Map<String, MediaSegmentDescriptor> segmentMap;
  private final List<List<StringDoublePair>> containerResults;
  private final List<Float> timeDistances;
  private final Float maxLength;

  /**
   * Constructor to create a temporal scoring instance.
   */
  public TemporalScoring(Map<String, MediaSegmentDescriptor> segmentMap, List<List<StringDoublePair>> containerResults, List<Float> timeDistances, Float maxLength) {
    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
    this.timeDistances = timeDistances;
    this.maxLength = maxLength;
  }

  /**
   * Begin scoring the information provided at the constructor with either the time distance method or sequential depending on time distances provided.
   */
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
