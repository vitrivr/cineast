package org.vitrivr.cineast.core.util;

import com.carrotsearch.hppc.ObjectDoubleMap;
import com.google.common.collect.ListMultimap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;

public class ScoreFusion {

  private static final Logger logger = LogManager.getLogger();

  private ScoreFusion() {
  }

  /**
   * Fuses the object scores into the segment scores by adding every object score to the scores of its segments. If an object without any of its segments was found, the first segment gets added and used instead. Note that this method <i>modifies {@code scoreBySegmentId} in place without changing {@code scoreByObjectId}</i>.
   *
   * @param scoreBySegmentId segment ids with their respective score
   * @param scoreByObjectId  object ids with their respective score
   */
  public static void fuseObjectsIntoSegments(ObjectDoubleMap<String> scoreBySegmentId,
      ObjectDoubleMap<String> scoreByObjectId, MediaSegmentReader mediaSegmentReader) {

    Set<String> objectIds = StreamSupport.stream(scoreByObjectId.keys().spliterator(), false).map(x -> x.value).collect(Collectors.toSet());
    if (objectIds.isEmpty()) {
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

  private static void fuseObjectScoreIntoSegments(ObjectDoubleMap<String> scoreBySegmentId,
      double objectScore, List<MediaSegmentDescriptor> segments) {
    boolean objectSegmentsFoundInResults = false;
    for (MediaSegmentDescriptor segment : segments) {
      if (scoreBySegmentId.containsKey(segment.getSegmentId())) {
        scoreBySegmentId.addTo(segment.getSegmentId(), objectScore);
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
