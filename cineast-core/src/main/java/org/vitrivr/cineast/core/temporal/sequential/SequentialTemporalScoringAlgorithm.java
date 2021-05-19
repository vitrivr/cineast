package org.vitrivr.cineast.core.temporal.sequential;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.temporal.AbstractTemporalScoringAlgorithm;
import org.vitrivr.cineast.core.temporal.ScoredSegment;

/**
 * Implementation of the sequential scoring algorithm to score a result set from a temporal query.
 *
 * <p>Scores the {@link StringDoublePair}s in their container order</p>
 * <p>Scores without provided time distances between the segments</p>
 * <p>The Algorithm builds temporal paths by creating paths from a starting segment and creating a path from this segment and putting it in a queue. While the queue is not empty it then pops a path from the queue and creates new paths for each of the segments following the last to the path added segment.</p>
 * <p>It then saves the best path created in the before mentioned fashion and scores a segment according to this.</p>
 */
public class SequentialTemporalScoringAlgorithm extends AbstractTemporalScoringAlgorithm {

  private final Map<String, List<SequentialPath>> objectPaths;

  public SequentialTemporalScoringAlgorithm(Map<String, MediaSegmentDescriptor> segmentMap, List<List<StringDoublePair>> containerResults, float maxLength) {
    super(segmentMap, containerResults, maxLength);
    this.objectPaths = new HashMap<>();
  }

  /**
   * Score the information given to the class upon the creation of the class according to the sequential scoring algorithm.
   *
   * @return List of {@link TemporalObject}
   */
  @Override
  public List<TemporalObject> score() {
    /* Calculate the best path for every segment in the result set given to the class. */
    for (Map<Integer, ScoredSegment> segments : scoredSegmentStorage.values()) {
      for (ScoredSegment scoredSegment : segments.values()) {
        MediaSegmentDescriptor mediaSegmentDescriptor = segmentMap.get(scoredSegment.getSegmentId());
        SequentialPath sequentialPath = this.getBestPathForSegment(mediaSegmentDescriptor, scoredSegment);

        objectPaths.putIfAbsent(mediaSegmentDescriptor.getObjectId(), new ArrayList<>());
        this.objectPaths.get(mediaSegmentDescriptor.getObjectId()).add(sequentialPath);
      }
    }

    List<TemporalObject> results = new ArrayList<>();

    /*
    Calculate the max value of a segment of every objectId and remove duplicates from the result list and sort by segmentId. We are maxpooling scores and declare all segments of an object as a path and we ignore all previous information from the individual paths of all segments.
     */
    this.objectPaths.forEach((objectId, paths) -> {
      double max = paths
          .stream()
          .mapToDouble(n -> (n.getScore() / (this.maxContainerId + 1)))
          .max()
          .orElse(-1D);
      List<String> segmentIds = paths.stream()
          .flatMap(listContainer -> listContainer.getSegmentIds().stream())
          .sorted()
          .distinct().collect(Collectors.toList());
      TemporalObject temporalObject = new TemporalObject(segmentIds, objectId, max);
      if (max > 0d) {
        results.add(temporalObject);
      }
    });

    /* Return the sorted temporal objects. */
    return results.stream()
        .sorted(Comparator.comparingDouble(TemporalObject::getScore).reversed())
        .collect(Collectors.toList());
  }

  /* Calculate the best path possible for a segment. */
  private SequentialPath getBestPathForSegment(MediaSegmentDescriptor mediaSegmentDescriptor, ScoredSegment scoredSegment) {
    PriorityQueue<SequentialPath> pathQueue = new PriorityQueue<>();

    SequentialPath initPath = new SequentialPath(mediaSegmentDescriptor.getObjectId(), scoredSegment);

    SequentialPath bestPath = initPath;
    pathQueue.add(initPath);

    /* Create new paths until there are no more possible paths for a segment. */
    while (!pathQueue.isEmpty()) {
      SequentialPath path = pathQueue.remove();

      ScoredSegment lastHighestSegment = path.getCurrentLastSegment();

      /* Get the potential following segments from the scored segments sets tree set */
      Set<ScoredSegment> potentialFollowingSegments = scoredSegmentSets.get(mediaSegmentDescriptor.getObjectId()).tailSet(lastHighestSegment);
      for (ScoredSegment candidate : potentialFollowingSegments) {
        /*
        Only look at a candidate if their containerId is higher than the current one. Due to the
        compareTo of ScoredSegment that allows to classify segments with the same containerId but
        higher segmentId as being higher.
         */
        if (candidate.getContainerId() <= lastHighestSegment.getContainerId() || candidate.getStartAbs() < lastHighestSegment.getEndAbs()) {
          continue;
        }
        /*
         If we have reached the end of possible temporal paths we either store it and a new best
         if it is shorter than max length and has a higher score or ignore the path.
        */
        if (candidate.getContainerId() == this.maxContainerId) {
          if ((bestPath.getScore() / (maxContainerId + 1)) < (candidate.getScore() / (maxContainerId + 1)) + path.getScore() && candidate.getEndAbs() - path.getStartAbs() <= this.maxLength) {
            bestPath = new SequentialPath(path);
            bestPath.addSegment(candidate);
          }
          continue;
        }
        /*
         Otherwise we look if the candidate together with the existing path is valid (shorter
         than max length) and if yes maybe update best path if it is better and then add it to
         the queue to be reevaluated in the next round as a longer path that is potentially
         better.
        */
        if (candidate.getEndAbs() - path.getStartAbs() <= this.maxLength) {
          SequentialPath candidatePath = new SequentialPath(path);
          candidatePath.addSegment(candidate);
          if ((bestPath.getScore() / (maxContainerId + 1)) < (candidatePath.getScore() / (maxContainerId + 1))) {
            bestPath = candidatePath;
          }
          pathQueue.add(candidatePath);
        }
      }
    }
    return bestPath;
  }
}
