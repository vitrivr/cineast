package org.vitrivr.cineast.core.util;

import com.google.common.collect.ListMultimap;
import gnu.trove.map.TObjectDoubleMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;

import java.util.List;
import java.util.Set;

public class ScoreFusion {
  private ScoreFusion() {}

  private static final Logger logger = LogManager.getLogger();

  /**
   * Fuses the object scores into the segment scores by adding every object score to the scores of
   * its segments. If an object without any of its segments was found, the first segment gets added
   * and used instead.
   * Note that this method <i>modifies {@code scoreBySegmentId} in place without changing
   * {@code scoreByObjectId}</i>.
   *
   * @param scoreBySegmentId segment ids with their respective score
   * @param scoreByObjectId object ids with their respective score
   */
  public static void fuseObjectsIntoSegments(TObjectDoubleMap<String> scoreBySegmentId,
      TObjectDoubleMap<String> scoreByObjectId, MediaSegmentReader mediaSegmentReader) {

    Set<String> objectIds = scoreByObjectId.keySet();
    if(objectIds.isEmpty()){
      return;
    }
    ListMultimap<String, MediaSegmentDescriptor> segmentsByObjectId = mediaSegmentReader.lookUpSegmentsOfObjects(objectIds);
    for (String objectId : segmentsByObjectId.keySet()) {
      assert scoreByObjectId.containsKey(objectId);
      double objectScore = scoreByObjectId.get(objectId);
      List<MediaSegmentDescriptor> segments = segmentsByObjectId.get(objectId);
      if (segments.isEmpty()) {
        logger.error("Object {} has no segments", objectId);
        continue;
      }
      fuseObjectScoreIntoSegments(scoreBySegmentId, objectScore, segments);
    }
  }

  private static void fuseObjectScoreIntoSegments(TObjectDoubleMap<String> scoreBySegmentId,
      double objectScore, List<MediaSegmentDescriptor> segments) {
    boolean objectSegmentsFoundInResults = false;
    for (MediaSegmentDescriptor segment : segments) {
      boolean foundElement = scoreBySegmentId.adjustValue(segment.getSegmentId(), objectScore);
      if (foundElement) {
        objectSegmentsFoundInResults = true;
      }
    }

    if (!objectSegmentsFoundInResults) {
      MediaSegmentDescriptor firstSegment = segments.get(0);
      String firstId = firstSegment.getSegmentId();
      scoreBySegmentId.put(firstId, objectScore);
    }
  }
}
