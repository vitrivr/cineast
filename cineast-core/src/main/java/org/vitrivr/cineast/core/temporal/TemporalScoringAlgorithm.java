package org.vitrivr.cineast.core.temporal;

import java.util.ArrayList;
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
 *
 * @author vGsteiger
 * @created 12.05.2021
 */
public abstract class TemporalScoringAlgorithm {

  protected final Map<String, MediaSegmentDescriptor> segmentMap;
  protected final Map<String, TreeSet<ScoredSegment>> scoredSegmentSets;
  protected final Map<String, List<ScoredSegment>> scoredSegmentStorage;
  protected final float maxLength;
  protected final int maxContainerId;

  protected TemporalScoringAlgorithm(Map<String, MediaSegmentDescriptor> segmentMap, List<List<StringDoublePair>> containerResults, float maxLength) {
    this.segmentMap = segmentMap;
    this.scoredSegmentSets = new HashMap<>();
    this.scoredSegmentStorage = new HashMap<>();
    this.maxLength = maxLength;
    this.maxContainerId = containerResults.size() - 1;

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
              scoredSegmentStorage.get(segmentDescriptor.getSegmentId()).get(currentContainerId).addScore(stringDoublePair);
            } catch (IndexOutOfBoundsException e) {
              scoredSegmentStorage.get(segmentDescriptor.getSegmentId()).add(currentContainerId, new ScoredSegment(segmentDescriptor, stringDoublePair.value, currentContainerId, (segmentDescriptor.getEndabs() - segmentDescriptor.getStartabs())));
            }
          } else {
            List<ScoredSegment> segmentList = new ArrayList<>();
            segmentList.add(new ScoredSegment(segmentDescriptor, stringDoublePair.value, currentContainerId, (segmentDescriptor.getEndabs() - segmentDescriptor.getStartabs())));
            scoredSegmentStorage.put(segmentDescriptor.getSegmentId(), segmentList);
          }
        }
      }
    }

    /* Assign the scored segments to the tree sets corresponding to their objectId. */
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

  public abstract List<TemporalObject> score();

}
