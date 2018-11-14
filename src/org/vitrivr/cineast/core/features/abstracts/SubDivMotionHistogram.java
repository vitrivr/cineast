package org.vitrivr.cineast.core.features.abstracts;

import java.util.List;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.features.extractor.Extractor;

public abstract class SubDivMotionHistogram extends MotionHistogramCalculator implements Extractor {
  protected PersistencyWriter<?> phandler;
  
  protected SubDivMotionHistogram(String tableName, String fieldName, double maxDist) {
    super(tableName, fieldName, (float) maxDist);
  }

  @Override
  public void init(PersistencyWriterSupplier supply) {
    this.phandler = supply.get();
    this.phandler.open(this.tableName);
    this.phandler.setFieldNames("id", "sums", "hist");
  }

  protected void persist(String shotId, ReadableFloatVector fs1, ReadableFloatVector fs2) {
    PersistentTuple tuple = this.phandler.generateTuple(shotId, fs1, fs2);
    this.phandler.persist(tuple);
  }

  @Override
  protected List<ScoreElement> getSimilar(float[] vector, ReadableQueryConfig qc) {
    ReadableQueryConfig rqc = setQueryConfig(qc);
    List<SegmentDistanceElement> distances = this.selector
        .getNearestNeighboursGeneric(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), vector,
            "hist", SegmentDistanceElement.class, qc);
    return DistanceElement.toScore(distances, rqc.getCorrespondenceFunction().get());
  }

  @Override
  public void finish() {
    if (this.phandler != null) {
      this.phandler.close();
    }
    super.finish();
  }
}
