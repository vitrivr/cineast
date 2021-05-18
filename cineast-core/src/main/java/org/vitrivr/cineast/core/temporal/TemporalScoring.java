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
   *
   * @param segmentMap       map of the segment ids and their respective {@link MediaSegmentDescriptor}s to retrieve the necessary information for temporal scoring
   * @param containerResults list of lists of the retrieval results where at the index of the outer list we have the result list of the corresponding temporal query in sequence
   * @param timeDistances    time distances between the temporal segments, may be empty
   * @param maxLength        the maximal length of the temporal sequences to be scored
   */
  public TemporalScoring(Map<String, MediaSegmentDescriptor> segmentMap, List<List<StringDoublePair>> containerResults, List<Float> timeDistances, Float maxLength) {
    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
    this.timeDistances = timeDistances;
    this.maxLength = maxLength;
  }

  /**
   * Score the information provided at the constructor with either the time distance method or sequential depending on time distances provided.
   */
  public List<TemporalObject> score() {
    AbstractTemporalScoringAlgorithm temporalScoringAlgorithm;
    float maxLength = this.maxLength;
    if (maxLength < 0) {
      maxLength = Float.MAX_VALUE;
    }
    if (this.timeDistances.size() > 0) {
      temporalScoringAlgorithm = new TimeDistanceTemporalScoringAlgorithm(this.segmentMap, this.containerResults, this.timeDistances, maxLength);
    } else {
      temporalScoringAlgorithm = new SequentialTemporalScoringAlgorithm(this.segmentMap, this.containerResults, maxLength);
    }
    return temporalScoringAlgorithm.score();
  }
}
