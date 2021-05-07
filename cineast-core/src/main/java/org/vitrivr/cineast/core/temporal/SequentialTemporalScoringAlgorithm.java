package org.vitrivr.cineast.core.temporal;

import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

public class SequentialTemporalScoringAlgorithm implements TemporalScoringAlgorithm {

  private final Map<String, MediaObjectDescriptor> objectMap;
  private final Map<String, MediaSegmentDescriptor> segmentMap;
  private final List<List<StringDoublePair>> containerResults;
  private final Float maxLength;

  SequentialTemporalScoringAlgorithm(Map<String, MediaObjectDescriptor> objectMap,
      Map<String, MediaSegmentDescriptor> segmentMap,
      List<List<StringDoublePair>> containerResults,
      Float maxLength) {
    this.objectMap = objectMap;
    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
    this.maxLength = maxLength;
  }

  @Override
  public List<TemporalObject> score() {
    return null;
  }
}
