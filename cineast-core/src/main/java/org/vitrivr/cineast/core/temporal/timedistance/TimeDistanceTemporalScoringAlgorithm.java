package org.vitrivr.cineast.core.temporal.timedistance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.temporal.AbstractTemporalScoringAlgorithm;
import org.vitrivr.cineast.core.temporal.ScoredSegment;

/**
 * Implementation of the time distance scoring algorithm to score a result set from a temporal query.
 *
 * <p>Scores the {@link StringDoublePair}s in their container order</p>
 * <p>Scores with provided time distances between the segments</p>
 * <p>The {@link TimeDistanceTemporalScoringAlgorithm} iteratively builds temporal sequences from the result containers corresponding to a temporal query that are in the right order and with the correct distance. We penalize segments that are not within the correct distance with an inverse decay function.</p>
 * <p>If a segment is missing in the result containers, this segment simply gets skipped and the next suitable segment is looked for.</p>
 */
public class TimeDistanceTemporalScoringAlgorithm extends AbstractTemporalScoringAlgorithm {

  private final List<Float> timeDistances;

  public TimeDistanceTemporalScoringAlgorithm(Map<String, MediaSegmentDescriptor> segmentMap, List<List<StringDoublePair>> containerResults, Float maxLength, List<Float> timeDistances) {
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
    Map<String, ResultStorage> resultMap = new HashMap<>();
    this.scoredSegmentSets.values().forEach(set -> {
      set.forEach(segment -> {
        TemporalObject best = getBestTemporalObject(segment);
        resultMap.putIfAbsent(best.getObjectId(), new ResultStorage(best.getObjectId()));
        List<String> bestSegmentIds = best.getSegments();
        List<Float> bestStartAbs = this.getStartAbs(bestSegmentIds);
        resultMap.get(best.getObjectId()).addSegmentsAndScore(IntStream.range(0, bestSegmentIds.size()).boxed().collect(Collectors.toMap(bestStartAbs::get, bestSegmentIds::get)), best.getScore());
      });
    });
    Stream<TemporalObject> resultStream;
    resultStream = resultMap.values().stream().map(ResultStorage::toTemporalObject);
    /* Return the sorted temporal objects. */
    return resultStream
        .sorted(Comparator.comparingDouble(TemporalObject::getScore).reversed())
        .collect(Collectors.toList());
  }

  private TemporalObject getBestTemporalObject(ScoredSegment item) throws NoSuchElementException {
    List<String> segments = new ArrayList<>();
    segments.add(item.getSegmentId());
    MediaSegmentDescriptor currentDescriptor = segmentMap.get(item.getSegmentId());

    /* The following shouldn't happen, if it does nonetheless please let the developers know */
    if (currentDescriptor == null) {
      throw new NoSuchElementException("Item with segmentId " + item.getSegmentId() + " was not provided for scoring");
    }
    double score = item.getScore();

    float currentEndAbs = currentDescriptor.getEndabs();

    ScoredSegment currentSegment = new ScoredSegment(item);

    /* Go through all container ids and calculate the element that maximizes the score of the element */
    for (int innerContainerId = item.getContainerId() + 1; innerContainerId <= this.maxContainerId; innerContainerId++) {
      final int lambdaInnerId = innerContainerId;
      /* Get the set of values with a higher container id or a higher segment id. */
      SortedSet<ScoredSegment> setFromElement = this.scoredSegmentSets.get(currentSegment.getObjectId()).tailSet(item);
      List<ScoredSegment> candidates = setFromElement.stream().filter(c -> c.getContainerId() == lambdaInnerId).filter(c -> c.getEndAbs() - item.getStartAbs() >= 0).filter(c -> c.getEndAbs() - item.getStartAbs() <= this.maxLength).filter(c -> !c.getSegmentId().equals(item.getSegmentId())).collect(Collectors.toList());
      if (candidates.size() == 0) {
        continue;
      }
      ScoredSegment bestSegment = null;
      double bestScore = 0D;

      /* Go through all candidates from the currentSegment */
      for (ScoredSegment candidate : candidates) {
        MediaSegmentDescriptor innerDescriptor = this.segmentMap.get(candidate.getSegmentId());
        if (innerDescriptor == null) {
          continue;
        }
        /* Calculate the inverse decay score of the candidate */
        double innerScore = calculateInverseDecayScore(currentEndAbs, candidate, this.timeDistances.get(innerContainerId - 1), innerDescriptor);
        if (innerScore > bestScore) {
          bestScore = innerScore;
          bestSegment = candidate;
        }
      }
      /* If there is a best segment, store it and update current End abs, otherwise update end abs with time distances */
      if (bestSegment != null) {
        currentSegment = bestSegment;
        currentEndAbs = this.segmentMap.get(bestSegment.getSegmentId()).getEndabs();
        segments.add(bestSegment.getSegmentId());
      } else {
        currentEndAbs += timeDistances.get(innerContainerId - 1);
      }
      score += bestScore;
    }

    return new TemporalObject(segments, item.getObjectId(), score / (this.maxContainerId + 1));
  }

  private double calculateInverseDecayScore(float currentSegmentEndTime, ScoredSegment nextSegment, float timeDifference, MediaSegmentDescriptor segmentDescriptor) {
    if (segmentDescriptor.getStartabs() >= currentSegmentEndTime && segmentDescriptor.getStartabs() < currentSegmentEndTime + timeDifference) {
      return Math.exp((0.1f * (segmentDescriptor.getStartabs() - currentSegmentEndTime - timeDifference))) * nextSegment.getScore();
    }
    if (segmentDescriptor.getStartabs() >= currentSegmentEndTime && segmentDescriptor.getStartabs() > currentSegmentEndTime + timeDifference) {
      return Math.exp((-0.1f * (segmentDescriptor.getStartabs() - currentSegmentEndTime - timeDifference))) * nextSegment.getScore();
    }
    if (segmentDescriptor.getStartabs() == currentSegmentEndTime + timeDifference) {
      return nextSegment.getScore();
    }
    return 0D;
  }

  /* Storage class for the results for easier result transformation. */
  private static class ResultStorage {

    /* Mapping the startAbs to the segmentId, we utilise a tree map to easily retrieve the values sorted by key */
    private Map<Float, String> segments = new TreeMap<>();
    private double score = 0D;
    private final String objectId;

    public ResultStorage(String objectId) {
      this.objectId = objectId;
    }

    public void addSegmentsAndScore(Map<Float, String> segments, double update) {
      this.segments.putAll(segments);
      if (this.score < update) {
        this.score = update;
      }
    }

    public TemporalObject toTemporalObject() {
      return new TemporalObject(new ArrayList<>(segments.values()), this.objectId, this.score);
    }

    public Map<Float, String> getSegments() {
      return new HashMap<>(segments);
    }

    public double getScore() {
      return score;
    }

    public String getObjectId() {
      return objectId;
    }
  }
}
