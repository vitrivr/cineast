package org.vitrivr.cineast.core.temporal;

import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

public class TimeDistanceTemporalScoringAlgorithm implements TemporalScoringAlgorithm {

  private final Map<String, MediaObjectDescriptor> objectMap;
  private final Map<String, MediaSegmentDescriptor> segmentMap;
  private final List<List<StringDoublePair>> containerResults;
  private final List<Float> timeDistances;
  private final Float maxLength;

  TimeDistanceTemporalScoringAlgorithm(Map<String, MediaObjectDescriptor> objectMap,
      Map<String, MediaSegmentDescriptor> segmentMap,
      List<List<StringDoublePair>> containerResults,
      List<Float> timeDistances,
      Float maxLength) {
    this.objectMap = objectMap;
    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
    this.timeDistances = timeDistances;
    this.maxLength = maxLength;
  }

  @Override
  public List<TemporalObject> score() {
    return null;
  }
}
