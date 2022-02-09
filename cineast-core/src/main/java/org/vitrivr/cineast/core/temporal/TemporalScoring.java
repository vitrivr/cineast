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
 * <p>The currently two different temporal scoring algorithms, sequential and time distance, differ in the usage of the user provided time distances</p>
 * <p>Sequential temporal scoring builds sequences of {@link ScoredSegment}s that satisfy the order provided by the similarity queries of the temporal query where it builds the best possible paths and adds up the scores of the participating segments to calculate an aggregated score</p>
 * <p>Time Distance temporal scoring also builds sequences of objects that have the same order than the similarity queries, however, it additionally provides the means to the user to define time distances between the segments and penalizes sequences that do not have the correct time distances between the segments</p>
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
    float maxLength = Float.MAX_VALUE;
    if (this.maxLength != null && this.maxLength > 0) {
      maxLength = this.maxLength;
    }
    if (this.timeDistances.size() > 0) {
      temporalScoringAlgorithm = new TimeDistanceTemporalScoringAlgorithm(this.segmentMap, this.containerResults, maxLength, this.timeDistances);
    } else {
      temporalScoringAlgorithm = new SequentialTemporalScoringAlgorithm(this.segmentMap, this.containerResults, maxLength);
    }
    return temporalScoringAlgorithm.score();
  }
}
