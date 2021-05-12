package org.vitrivr.cineast.core.temporal.timedistance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.temporal.ScoredSegment;
import org.vitrivr.cineast.core.temporal.TemporalScoringAlgorithm;

/**
 * Implementation of the time distance scoring algorithm to score a result set from a temporal query.
 *
 * <p>Scores the {@link StringDoublePair}s in their container order</p>
 * <p>Scores with provided time distances between the segments</p>
 *
 * @author vGsteiger
 * @created 12.05.2021
 */
public class TimeDistanceTemporalScoringAlgorithm extends TemporalScoringAlgorithm {

  private final List<Float> timeDistances;

  public TimeDistanceTemporalScoringAlgorithm(Map<String, MediaSegmentDescriptor> segmentMap, List<List<StringDoublePair>> containerResults, List<Float> timeDistances, Float maxLength) {
    super(segmentMap, containerResults, maxLength);
    this.timeDistances = timeDistances;
  }

  /**
   * Score the information given to the class upon the creation of the class according to the sequential scoring algorithm.
   *
   * @return List of {@link TemporalObject}
   */
  @Override
  public List<TemporalObject> score() {
    /* Calculate the best temporal object linearly for all segments given and assigned in the constructor. */
    List<TemporalObject> results = new ArrayList<>();
    for (TreeSet<ScoredSegment> set : this.scoredSegmentSets.values()) {
      for (ScoredSegment segment : set) {
        TemporalObject best = getBestTemporalObject(segment, segment.getContainerId());
        results.add(best);
      }
    }
    /* Return the sorted temporal objects. */
    return results.stream()
        .sorted(Comparator.comparingDouble(TemporalObject::getScore))
        .collect(Collectors.toList());
  }

  private TemporalObject getBestTemporalObject(ScoredSegment item, int containerId) throws NoSuchElementException {
    List<String> segments = new ArrayList<>();
    segments.add(item.getSegmentId());
    MediaSegmentDescriptor currentDescriptor = segmentMap.get(item.getSegmentId());

    /* The following shouldn't happen, if it does nonetheless please let the developers know */
    if (currentDescriptor == null) {
      throw new NoSuchElementException("Item with segmentId " + item.getSegmentId() + " was not provided for scoring");
    }
    double score = item.getScore();

    float currentEndAbs = currentDescriptor.getEndabs();
    float currentTemporalObjectLength = item.getSegmentLength();

    ScoredSegment currentSegment = new ScoredSegment(item);

    /* Go through all container ids and calculate the element that maximizes the score of the element */
    for (int innerContainerId = containerId + 1; innerContainerId <= this.maxContainerId; innerContainerId++) {

      /* Get the set of values with a higher container id or a higher segment id. This has to be redone as currentSegment may change if a fitting segment has been found */
      SortedSet<ScoredSegment> setFromElement = this.scoredSegmentSets.get(currentSegment.getObjectId()).tailSet(item);
      ScoredSegment bestSegment = null;
      double bestScore = 0D;

      /* Go through all candidates from the currentSegment */
      for (ScoredSegment candidate : setFromElement) {
        if (candidate.getContainerId() == innerContainerId && candidate.getSegmentLength() + currentTemporalObjectLength < this.maxLength) {
          MediaSegmentDescriptor innerDescriptor = this.segmentMap.get(candidate.getSegmentId());
          if (innerDescriptor == null) {
            continue;
          }
          double innerScore = calculateInverseDecayScore(currentEndAbs, candidate, this.timeDistances.get(innerContainerId - 1), innerDescriptor);
          if (innerScore > bestScore) {
            bestSegment = candidate;
          }
        }
      }
      if (bestSegment != null) {
        currentSegment = bestSegment;
        currentEndAbs = this.segmentMap.get(bestSegment.getSegmentId()).getEndabs();
        segments.add(bestSegment.getSegmentId());
        currentTemporalObjectLength += bestSegment.getSegmentLength();
      } else {
        currentEndAbs += timeDistances.get(innerContainerId - 1);
      }
      score += bestScore;
    }

    return new TemporalObject(segments, item.getObjectId(), score);
  }

  private double calculateInverseDecayScore(float currentSegmentEndTime, ScoredSegment nextSegment, float timeDifference, MediaSegmentDescriptor segmentDescriptor) {
    double score = 0D;
    if (segmentDescriptor.getStartabs() >= currentSegmentEndTime && segmentDescriptor.getStartabs() < currentSegmentEndTime + timeDifference) {
      score = Math.exp((0.1f * (segmentDescriptor.getStartabs() - currentSegmentEndTime - timeDifference))) * nextSegment.getScore();
    } else if (segmentDescriptor.getStartabs() >= currentSegmentEndTime && segmentDescriptor.getStartabs() > currentSegmentEndTime + timeDifference) {
      score = Math.exp((-0.1f * (segmentDescriptor.getStartabs() - currentSegmentEndTime - timeDifference))) * nextSegment.getScore();
    } else if (segmentDescriptor.getStartabs() == currentSegmentEndTime + timeDifference) {
      score = nextSegment.getScore();
    }
    return score;
  }
}
