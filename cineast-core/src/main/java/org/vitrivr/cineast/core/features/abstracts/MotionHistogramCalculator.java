package org.vitrivr.cineast.core.features.abstracts;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import georegression.struct.point.Point2D_F32;

import java.util.*;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;

import java.util.function.Supplier;

public abstract class MotionHistogramCalculator implements Retriever {

  protected DBSelector selector;
  protected final float maxDist;
  protected final CorrespondenceFunction linearCorrespondence;
  protected final String tableName;
  protected final String fieldName;
  private final int vectorLength;

  protected MotionHistogramCalculator(String tableName, String fieldName, float maxDist, int cells) {
    this.maxDist = maxDist;
    this.linearCorrespondence = CorrespondenceFunction.linear(this.maxDist);
    this.tableName = tableName;
    this.fieldName = fieldName;
    this.vectorLength = cells * cells;
  }

  @Override
  public void init(DBSelectorSupplier supply) {
    this.selector = supply.get();
    this.selector.open(tableName);
  }

  @Override
  public List<String> getTableNames() {
    return Collections.singletonList(tableName);
  }

  private static int getidx(int subdiv, float x, float y) {
    int ix = (int) Math.floor(subdiv * x);
    int iy = (int) Math.floor(subdiv * y);
    ix = Math.max(Math.min(ix, subdiv - 1), 0);
    iy = Math.max(Math.min(iy, subdiv - 1), 0);

    return ix * subdiv + iy;
  }

  protected Pair<List<Double>, ArrayList<ArrayList<Float>>> getSubDivHist(
      int subdiv, List<Pair<Integer, LinkedList<Point2D_F32>>> list) {

    double[] sums = new double[subdiv * subdiv];
    float[][] hists = new float[subdiv * subdiv][8];

    for (Pair<Integer, LinkedList<Point2D_F32>> pair : list) {
      LinkedList<Point2D_F32> path = pair.second;
      if (path.size() > 1) {
        Iterator<Point2D_F32> iter = path.iterator();
        Point2D_F32 last = iter.next();
        while (iter.hasNext()) {
          Point2D_F32 current = iter.next();
          double dx = current.x - last.x;
          double dy = current.y - last.y;
          int idx = ((int) Math.floor(4 * Math.atan2(dy, dx)
              / Math.PI) + 4) % 8;
          double len = Math.sqrt(dx * dx + dy * dy);
          hists[getidx(subdiv, last.x, last.y)][idx] += len;
          last = current;
        }
      }
    }

    for (int i = 0; i < sums.length; ++i) {
      float[] hist = hists[i];
      double sum = 0;
      for (int j = 0; j < hist.length; ++j) {
        sum += hist[j];
      }
      if (sum > 0) {
        for (int j = 0; j < hist.length; ++j) {
          hist[j] /= sum;
        }
        hists[i] = hist;
      }
      sums[i] = sum;
    }

    ArrayList<Double> sumList = new ArrayList<Double>(sums.length);
    for (double d : sums) {
      sumList.add(d);
    }

    ArrayList<ArrayList<Float>> histList = new ArrayList<ArrayList<Float>>(
        hists.length);
    for (float[] hist : hists) {
      ArrayList<Float> h = new ArrayList<Float>(8);
      for (float f : hist) {
        h.add(f);
      }
      histList.add(h);
    }

    return new Pair<List<Double>, ArrayList<ArrayList<Float>>>(sumList,
        histList);
  }

  protected List<ScoreElement> getSimilar(float[] vector, ReadableQueryConfig qc) {
    ReadableQueryConfig qcc = setQueryConfig(qc);
    List<SegmentDistanceElement> distances = this.selector.getNearestNeighboursGeneric(qcc.getResultsPerModule(), vector, this.fieldName, SegmentDistanceElement.class, qcc);
    return DistanceElement.toScore(distances, qcc.getCorrespondenceFunction().get());
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    List<float[]> list = this.selector.getFeatureVectors(GENERIC_ID_COLUMN_QUALIFIER, new StringTypeProvider(segmentId), this.fieldName);
    if (list.isEmpty()) {
      return new ArrayList<>(1);
    }
    return getSimilar(list.get(0), qc);
  }

  @Override
  public void finish() {
    if (this.selector != null) {
      this.selector.close();
    }
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity(this.tableName, true, new AttributeDefinition("hist", AttributeType.VECTOR, vectorLength * 8), new AttributeDefinition("sums", AttributeType.VECTOR, vectorLength));
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().dropEntity(this.tableName);
  }

  protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
    return QueryConfig.clone(qc).setDistanceIfEmpty(Distance.chisquared)
        .setCorrespondenceFunctionIfEmpty(this.linearCorrespondence);
  }
}
