package org.vitrivr.cineast.core.temporal.sequential;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.temporal.TemporalScoringAlgorithm;

/**
 * Implementation of the sequential scoring algorithm to score a result set from a temporal query.
 *
 * <p>Scores the {@link StringDoublePair}s in their container order</p>
 * <p>Scores without provided time distances between the segments</p>
 *
 * @author vGsteiger
 * @created 11.05.2021
 */
public class SequentialTemporalScoringAlgorithm implements TemporalScoringAlgorithm {

  private final Map<String, MediaSegmentDescriptor> segmentMap;
  private final Map<String, TreeSet<ScoredSegment>> scoredSegmentSets;
  private final float maxLength;
  private final Map<String, List<SequentialPath>> objectPaths;
  private final Map<String, List<ScoredSegment>> scoredSegmentStorage;
  private final int maxContainerId;

  public SequentialTemporalScoringAlgorithm(
      Map<String, MediaSegmentDescriptor> segmentMap,
      List<List<StringDoublePair>> containerResults,
      float maxLength
  ) {
    this.segmentMap = segmentMap;
    this.maxLength = maxLength;
    this.objectPaths = new HashMap<>();
    this.maxContainerId = containerResults.size() - 1;

    scoredSegmentStorage = new HashMap<>();

    /*
    Assign the values to the scoredSegmentStorage to have a map of segmentIds to a list of
    ScoredSegments at the index of the containerId.
     */
    for (List<StringDoublePair> currentContainerResults : containerResults) {
      for (StringDoublePair stringDoublePair : currentContainerResults) {
        /*
        Get the current media segment descriptor to save the necessary information in the
        ScoredSegment.
        */
        MediaSegmentDescriptor segmentDescriptor = segmentMap.get(stringDoublePair.key);
        /*
        If there is such a segment as found in a string double pair, add this to the
        scoredSegmentStorage for later usage.
         */
        if (segmentDescriptor != null) {
          int currentContainerId = containerResults.indexOf(currentContainerResults);
          if (scoredSegmentStorage.containsKey(segmentDescriptor.getSegmentId())) {
            try {
              scoredSegmentStorage.get(segmentDescriptor.getSegmentId()).get(currentContainerId)
                  .addScore(stringDoublePair);
            } catch (IndexOutOfBoundsException e) {
              scoredSegmentStorage.get(segmentDescriptor.getSegmentId())
                  .add(currentContainerId,
                      new ScoredSegment(
                          stringDoublePair.key,
                          stringDoublePair.value,
                          currentContainerId,
                          (segmentDescriptor.getEndabs() - segmentDescriptor.getStartabs()))
                  );
            }
          } else {
            List<ScoredSegment> segmentList = new ArrayList<>();
            segmentList
                .add(
                    new ScoredSegment(
                        segmentDescriptor.getSegmentId(),
                        stringDoublePair.value,
                        currentContainerId,
                        (segmentDescriptor.getEndabs() - segmentDescriptor.getStartabs())
                    ));
            scoredSegmentStorage.put(segmentDescriptor.getSegmentId(), segmentList);
          }
        }
      }
    }

    /*
    Assign the scored segments to the tree sets corresponding to their objectId.
     */
    this.scoredSegmentSets = new HashMap<>();
    for (Map.Entry<String, List<ScoredSegment>> entry : scoredSegmentStorage.entrySet()) {
      String objectId = segmentMap.get(entry.getKey()).getObjectId();
      for (ScoredSegment scoredSegment : entry.getValue()) {
        if (this.scoredSegmentSets.containsKey(objectId)) {
          this.scoredSegmentSets.get(objectId).add(scoredSegment);
        } else {
          TreeSet<ScoredSegment> tmpSet = new TreeSet<>();
          tmpSet.add(scoredSegment);
          this.scoredSegmentSets.put(objectId, tmpSet);
        }
      }
    }
  }

  /**
   * Score the information given to the class upon the creation of the class according to the
   * sequential scoring algorithm.
   *
   * @return List of {@link TemporalObject}
   */
  @Override
  public List<TemporalObject> score() {
    /*
    Calculate the best path for every segment in the result set given to the class.
     */
    for (MediaSegmentDescriptor mediaSegmentDescriptor : segmentMap.values()) {
      for (ScoredSegment scoredSegment : scoredSegmentStorage
          .get(mediaSegmentDescriptor.getSegmentId())) {
        SequentialPath sequentialPath = this
            .getBestPathForSegment(mediaSegmentDescriptor, scoredSegment);
        if (this.objectPaths.containsKey(mediaSegmentDescriptor.getObjectId())) {
          this.objectPaths.get(mediaSegmentDescriptor.getObjectId()).add(sequentialPath);
        } else {
          List<SequentialPath> tmpList = new ArrayList<>();
          tmpList.add(sequentialPath);
          this.objectPaths.put(mediaSegmentDescriptor.getObjectId(), tmpList);
        }
      }
    }

    List<TemporalObject> results = new ArrayList<>();

    /*
    Calculate the max value of a segment of every objectId and remove duplicates from the result
    list and sort by segmentId.
     */
    for (Map.Entry<String, List<SequentialPath>> entry : this.objectPaths.entrySet()) {
      String objectId = entry.getKey();
      List<SequentialPath> paths = entry.getValue();
      double max = paths
          .stream()
          .mapToDouble(SequentialPath::getScore)
          .max().orElse(-1D);
      List<String> segmentIds = paths.stream()
          .flatMap(listContainer -> listContainer.getSegmentIds().stream()).sorted()
          .distinct().collect(
              Collectors.toList());
      TemporalObject temporalObject = new TemporalObject(segmentIds, objectId, max);
      results.add(temporalObject);
    }

    /*
    Return the sorted temporal objects.
     */
    return results.stream().sorted(
        Comparator.comparingDouble(TemporalObject::getScore))
        .collect(
            Collectors.toList());
  }

  /*
  Calculate the best path possible for a segment.
   */
  private SequentialPath getBestPathForSegment(
      MediaSegmentDescriptor mediaSegmentDescriptor,
      ScoredSegment scoredSegment
  ) {
    PriorityQueue<SequentialPath> pathQueue = new PriorityQueue<>();

    SequentialPath initPath = new SequentialPath(
        mediaSegmentDescriptor.getObjectId(),
        scoredSegment
    );

    SequentialPath bestPath = initPath;
    pathQueue.add(initPath);

    /*
    Create new paths until there are no more possible paths for a segment.
     */
    while (!pathQueue.isEmpty()) {
      SequentialPath path = pathQueue.remove();

      ScoredSegment lastHighestSegment = path.getCurrentLastSegment();

      /*
      Get the potential following segments from the scored segments sets tree set
       */
      Set<ScoredSegment> potentialFollowingSegments = scoredSegmentSets
          .get(mediaSegmentDescriptor.getObjectId()).tailSet(lastHighestSegment);
      for (ScoredSegment candidate : potentialFollowingSegments) {
        /*
        Only look at a candidate if their containerId is higher than the current one. Due to the
        compareTo of ScoredSegment that allows to classify segments with the same containerId but
        higher segmentId as being higher.
         */
        if (candidate.getContainerId() > lastHighestSegment.getContainerId()) {
          /*
          If we have reached the end of possible temporal paths we either store it and a new best
          if it is shorter than max length and has a higher score or ignore the path.
           */
          if (candidate.getContainerId() == this.maxContainerId) {
            if (bestPath.getScore() < candidate.getScore() + path.getScore()
                && path.getPathLength() + candidate.getSegmentLength() < this.maxLength) {
              bestPath = new SequentialPath(path);
              bestPath.addSegment(candidate);
            }
          } else {
            /*
            Otherwise we look if the candidate together with the existing path is valid (shorter
            than max length) and if yes maybe update best path if it is better and then add it to
            the queue to be reevaluated in the next round as a longer path that is potentially
            better.
             */
            if (path.getPathLength() + candidate.getSegmentLength() < this.maxLength) {
              SequentialPath candidatePath = new SequentialPath(path);
              candidatePath.addSegment(candidate);
              if (bestPath.getScore() < candidatePath.getScore()) {
                bestPath = candidatePath;
              }
              pathQueue.add(candidatePath);
            }
          }
        }
      }
    }
    return bestPath;
  }
}
