package org.vitrivr.cineast.core.temporal;

import io.netty.util.collection.IntObjectHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

/**
 * Abstract class with common objects and functions of all temporal scoring algorithms.
 *
 * <p>Assigns the necessary values and calculates preliminary maps</p>
 */
public abstract class AbstractTemporalScoringAlgorithm {

  /* Map the segment Id to its MediaSegmentDescriptor */
  protected final Map<String, MediaSegmentDescriptor> segmentMap;
  /* Map an object id to a TreeSet of the corresponding ScoredSegments. We utilize a Tree set instead of a Hashset because we are interested in the natural order of the ScoredSegments. We later want to retrieve all segments larger to a current segment and that is only possible in a data structure that provides ordering. */
  protected final Map<String, TreeSet<ScoredSegment>> scoredSegmentSets;
  /* Map segment Ids to a Map that maps container Ids of the corresponding temporal result container to ScoredSegments. This is due to the fact that a segment may be present in multiple result containers. */
  protected final Map<String, Map<Integer, ScoredSegment>> scoredSegmentStorage;
  protected final float maxLength;
  protected final int maxContainerId;

  protected AbstractTemporalScoringAlgorithm(Map<String, MediaSegmentDescriptor> segmentMap, List<List<StringDoublePair>> containerResults, float maxLength) {
    this.segmentMap = segmentMap;
    this.scoredSegmentSets = new HashMap<>();
    this.scoredSegmentStorage = new HashMap<>();
    this.maxLength = maxLength;
    this.maxContainerId = containerResults.size() - 1;

    /*
    Assign the values to the scoredSegmentStorage to have a map of segmentIds to a list of
    ScoredSegments at the index of the containerId.
     */
    int currentContainerId = 0;
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
        if (segmentDescriptor == null) {
          continue;
        }
        /*
        Else assign the scored segment to the ScoredSegment and if there is already a scored Segment present att this score to the one present.
         */
        scoredSegmentStorage.putIfAbsent(segmentDescriptor.getSegmentId(), new IntObjectHashMap<>());
        if (scoredSegmentStorage.get(segmentDescriptor.getSegmentId()).containsKey(currentContainerId)) {
          scoredSegmentStorage.get(segmentDescriptor.getSegmentId()).get(currentContainerId).addScore(stringDoublePair);
        } else {
          scoredSegmentStorage.get(segmentDescriptor.getSegmentId()).put(currentContainerId, new ScoredSegment(segmentDescriptor, stringDoublePair.value, currentContainerId, (segmentDescriptor.getEndabs() - segmentDescriptor.getStartabs())));
        }
      }
      currentContainerId++;
    }

    /* Assign the scored segments to the tree sets corresponding to their objectId. */
    for (Map.Entry<String, Map<Integer, ScoredSegment>> entry : scoredSegmentStorage.entrySet()) {
      String objectId = segmentMap.get(entry.getKey()).getObjectId();
      for (ScoredSegment scoredSegment : entry.getValue().values()) {
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

  public abstract List<TemporalObject> score();

}
