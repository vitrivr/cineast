package org.vitrivr.cineast.core.temporal.timedistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.temporal.TemporalScoringAlgorithm;

public class TimeDistanceTemporalScoringAlgorithm implements TemporalScoringAlgorithm {

  private final Map<String, MediaSegmentDescriptor> segmentMap;
  private final List<List<StringDoublePair>> containerResults;
  private final List<Float> timeDistances;
  private final Float maxLength;

  public TimeDistanceTemporalScoringAlgorithm(Map<String, MediaSegmentDescriptor> segmentMap, List<List<StringDoublePair>> containerResults, List<Float> timeDistances, Float maxLength) {
    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
    this.timeDistances = timeDistances;
    this.maxLength = maxLength;
  }

  @Override
  public List<TemporalObject> score() {
    return new ArrayList<>();
  }
}
