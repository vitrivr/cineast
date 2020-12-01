package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.abstracts.SubDivMotionHistogram;
import org.vitrivr.cineast.core.util.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MotionHistogramBackground extends SubDivMotionHistogram {

  public MotionHistogramBackground() {
    super("features_MotionHistogramBackground", FEATURE_COLUMN_QUALIFIER, MathHelper.SQRT2, 1);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (!phandler.idExists(shot.getId())) {

      Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, shot.getBgPaths());

      double sum = pair.first.get(0);
      FloatVectorImpl fv = new FloatVectorImpl(pair.second.get(0));

      persist(shot.getId(), sum, fv);
    }
  }

  protected void persist(String shotId, double sum, ReadableFloatVector fs) {
    PersistentTuple tuple = this.phandler.generateTuple(shotId, sum, fs);
    this.phandler.persist(tuple);
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, sc.getBgPaths());

    FloatVectorImpl fv = new FloatVectorImpl(pair.second.get(0));
    return getSimilar(ReadableFloatVector.toArray(fv), qc);
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity("features_MotionHistogramBackground", true,
        new AttributeDefinition("sum", AttributeType.FLOAT),
        new AttributeDefinition("hist", AttributeType.VECTOR));
  }


}
