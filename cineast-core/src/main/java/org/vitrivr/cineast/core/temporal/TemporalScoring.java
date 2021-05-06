package org.vitrivr.cineast.core.temporal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

public class TemporalScoring {

  private final Map<String, MediaObjectDescriptor> objectMap;
  private final Map<String, MediaSegmentDescriptor> segmentMap;
  private final List<List<StringDoublePair>> containerResults;

  public TemporalScoring(Map<String, MediaObjectDescriptor> objectMap,
      Map<String, MediaSegmentDescriptor> segmentMap,
      List<List<StringDoublePair>> containerResults,
      QueryConfig qconf,
      List<Float> timeDistances,
      Float maxLength) {
    this.objectMap = objectMap;
    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
  }

  // TODO: Replace void with correct return type
  public List<TemporalObject> score() {

    return new ArrayList<>();
  }

}
