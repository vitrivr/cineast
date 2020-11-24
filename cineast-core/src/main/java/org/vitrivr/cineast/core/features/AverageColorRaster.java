package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.FuzzyColorHistogramQuantizer;
import org.vitrivr.cineast.core.color.FuzzyColorHistogramQuantizer.Color;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ColorUtils;
import org.vitrivr.cineast.core.util.GridPartitioner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AverageColorRaster extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();

  public AverageColorRaster() {
    super("features_AverageColorRaster", 1, 1);
  }

  @Override
  public void init(PersistencyWriterSupplier supply, int batchSize) {
    super.init(supply, batchSize);
    this.phandler.setFieldNames(GENERIC_ID_COLUMN_QUALIFIER, "hist", "raster");
  }

  protected static int get(Color c) {
    switch (c) {
    case Black:
      return 0;
    case Blue:
      return 1;
    case Brown:
      return 2;
    case Cyan:
      return 3;
    case Green:
      return 4;
    case Grey:
      return 5;
    case Magenta:
      return 6;
    case Navy:
      return 7;
    case Orange:
      return 8;
    case Pink:
      return 9;
    case Red:
      return 10;
    case Teal:
      return 11;
    case Violet:
      return 12;
    case White:
      return 13;
    case Yellow:
      return 14;
    default:
      return -1;
    }
  }

  protected static Color get(int i) {
    switch (i) {
    case 0:
      return Color.Black;
    case 1:
      return Color.Blue;
    case 2:
      return Color.Brown;
    case 3:
      return Color.Cyan;
    case 4:
      return Color.Green;
    case 5:
      return Color.Grey;
    case 6:
      return Color.Magenta;
    case 7:
      return Color.Navy;
    case 8:
      return Color.Orange;
    case 9:
      return Color.Pink;
    case 10:
      return Color.Red;
    case 11:
      return Color.Teal;
    case 12:
      return Color.Violet;
    case 13:
      return Color.White;
    case 14:
      return Color.Yellow;
    default:
      return Color.Black;
    }
  }

  protected static Color get(float f) {
    return get(Math.round(f));
  }

  MultiImage getMultiImage(SegmentContainer shot) {
    return shot.getAvgImg();
  }

  Pair<float[], float[]> computeRaster(SegmentContainer shot) {
    MultiImage avg = getMultiImage(shot);
    int[] colors = avg.getColors();
    ArrayList<Integer> ints = new ArrayList<>(colors.length);
    for (int i : colors) {
      ints.add(i);
    }
    ArrayList<LinkedList<Integer>> partitions = GridPartitioner.partition(ints, avg.getWidth(),
        avg.getHeight(), 8, 8);

    float[] raster = new float[64];
    float[] hist = new float[15];

    for (int i = 0; i < 64; ++i) {
      LinkedList<Integer> list = partitions.get(i);
      int col = ColorUtils.getAvg(list);
      ReadableLabContainer lab = ColorConverter.cachedRGBtoLab(col);
      raster[i] = get(FuzzyColorHistogramQuantizer.quantize(lab));
      hist[(int) raster[i]]++;
    }
    return new Pair<float[], float[]>(hist, raster);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getAvgImg() == MultiImage.EMPTY_MULTIIMAGE) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      Pair<float[], float[]> pair = computeRaster(shot);
      persist(shot.getId(), new FloatVectorImpl(pair.first), new FloatVectorImpl(pair.second));
    }
  }

  protected void persist(String shotId, ReadableFloatVector fs1, ReadableFloatVector fs2) {
    PersistentTuple tuple = this.phandler.generateTuple(shotId, fs1, fs2);
    this.phandler.persist(tuple);
  }

  protected static double register(float[] query, float[] db) {
    double best = 0;
    if (query.length < 64 || db.length < 64) {
      return 0;
    }
    for (int xoff = -4; xoff <= 4; ++xoff) {
      for (int yoff = -4; yoff <= 4; ++yoff) {
        double score = 0;
        for (int x = 0; x < 8; ++x) {
          for (int y = 0; y < 8; ++y) {
            int x1 = x + xoff;
            int y1 = y + yoff;
            if (x1 >= 0 && x1 < 8 && y1 >= 0 && y1 < 8) {
              int idx1 = 8 * x + y;
              int idx2 = 8 * x1 + y1;
              score += score(query[idx1], db[idx2]);
            }
          }
        }
        best = Math.max(best, score);
      }
    }
    return best / 64d;
  }

  protected static double score(float f1, float f2) {
    float fmin = Math.min(f1, f2);
    float fmax = Math.max(f1, f2);
    Color c1 = get(fmin);
    Color c2 = get(fmax);
    if (c1 == c2) {
      return 1d;
    }
    switch (c1) {
    case Black:
      if (c2 == Color.Grey) {
        return 0.25;
      }
      break;
    case Blue:
      if (c2 == Color.Navy || c2 == Color.Violet) {
        return 0.5;
      }
      if (c2 == Color.Cyan) {
        return 0.25;
      }
      break;
    case Brown:
      if (c2 == Color.Grey) {
        return 0.5;
      }
      break;
    case Cyan:
      if (c2 == Color.White) {
        return 0.25;
      }
      break;
    case Green:
      if (c2 == Color.Teal) {
        return 0.5;
      }
      break;
    case Grey:
      if (c2 == Color.White || c2 == Color.Black) {
        return 0.125;
      }
      break;
    case Magenta:
      if (c2 == Color.Violet || c2 == Color.Pink) {
        return 0.5;
      }
      break;
    case Orange:
      if (c2 == Color.Red || c2 == Color.Yellow) {
        return 0.5;
      }
      break;
    case Pink:
      if (c2 == Color.Red) {
        return 0.5;
      }
      break;
    default:
      return 0;
    }
    return 0;
  }

  private List<ScoreElement> getSimilar(float[] raster, float[] hist, ReadableQueryConfig rqc) {
    final int limit = rqc.getResultsPerModule();
    final QueryConfig qc = new QueryConfig(rqc).setDistanceIfEmpty(Distance.chisquared);

    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getNearestNeighbourRows(limit * 5,
        hist, "hist", qc);

    ArrayList<ScoreElement> scores = new ArrayList<>(rows.size());
    for (Map<String, PrimitiveTypeProvider> map : rows) {
      String id = map.get(GENERIC_ID_COLUMN_QUALIFIER).getString();
      double score = register(raster, map.get("raster").getFloatArray());
      scores.add(new SegmentScoreElement(id, score));
    }

    scores.sort(ScoreElement.SCORE_COMPARATOR.reversed());
    if (scores.size() > limit) {
      return scores.subList(0, limit);
    } else {
      return scores;
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<float[], float[]> pair = computeRaster(sc);
    return getSimilar(pair.second, pair.first, qc);
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows(GENERIC_ID_COLUMN_QUALIFIER,  new StringTypeProvider(segmentId));

    if (rows.isEmpty()) {
      return new ArrayList<>(1);
    }
    Map<String, PrimitiveTypeProvider> map = rows.get(0);

    return getSimilar(map.get("raster").getFloatArray(), map.get("hist").getFloatArray(), qc);
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity(this.tableName, true, new AttributeDefinition("hist", AttributeType.VECTOR, 15), new AttributeDefinition("raster", AttributeType.VECTOR, 64));
  }

}
