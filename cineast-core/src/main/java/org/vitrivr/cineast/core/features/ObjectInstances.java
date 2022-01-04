package org.vitrivr.cineast.core.features;

import java.util.List;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;


public class ObjectInstances extends AbstractFeatureModule {

  public static final String TABLE_NAME = "features_ObjectInstances";
  public static final int VECTOR_LENGTH = 512;

  public ObjectInstances() {
    super(TABLE_NAME, 1000f, //TODO figure out proper value
        VECTOR_LENGTH);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    //TODO implement extraction
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    //TODO implement extraction
    throw new UnsupportedOperationException();
  }
}
