package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import boofcv.abst.distort.FDistort;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.struct.image.GrayF32;
import com.googlecode.javaewah.datastructure.BitSet;
import java.util.function.Supplier;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.util.FastMath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.BitSetTypeProvider;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.writer.SimpleBitSetWriter;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

import java.util.List;

/**
 * Simple Image Fingerprinting Feature. Re-implemented based on the following two papers:
 *
 * Christoph Zauner. Implementation and benchmarking of perceptual image hash func- tions. Master’s thesis, University of Applied Sciences Hagenberg, Austria, 2010. Baris Coskun and Bulent Sankur. Robust video hash extraction. In Signal Processing Conference, 2004 12th European, pages 2295–2298. IEEE, 2004.
 *
 * @author silvan on 18.12.17.
 */
public class DCTImageHash extends AbstractFeatureModule {

  private static final String TABLE_NAME = "features_DCTimagehash";
  private static final Logger LOGGER = LogManager.getLogger();

  private static final DMatrixRMaj DCT_MATRIX;
  private static final DMatrixRMaj DCT_MATRIX_TRANSPOSED;
  private SimpleBitSetWriter writer;

  /**
   * DCT Matrix size
   */
  private static final int N = 32;

  static {
    DCT_MATRIX = new DMatrixRMaj(N, N);
    for (int x = 0; x < N; x++) {
      for (int y = 0; y < N; y++) {
        DCT_MATRIX.set(x, y, FastMath.sqrt(2f / N) * FastMath
            .cos(((2 * y + 1) * x * Math.PI) / (2 * N)));
      }
    }
    DCT_MATRIX_TRANSPOSED = new DMatrixRMaj(N, N);
    for (int x = 0; x < N; x++) {
      for (int y = 0; y < N; y++) {
        DCT_MATRIX_TRANSPOSED.set(x, y, DCT_MATRIX.get(y, x));
      }
    }
  }

  private final GrayF32 resizedImg = new GrayF32(32, 32);
  private final DMatrixRMaj resizedMat = new DMatrixRMaj(N, N);
  private final DMatrixRMaj intermediateMat = new DMatrixRMaj(N, N);
  private final DMatrixRMaj outputMat = new DMatrixRMaj(N, N);

  /**
   * No-args constructor for reflection
   */
  public DCTImageHash() {
    super(TABLE_NAME, 64, 64);
  }

  private BitSet extractHash(MultiImage image) {
    long start = System.nanoTime();
    //TODO Benchmark optimizations
    float[] luminance = ColorConverter.RGBtoLuminance(image.getColors());
    GrayF32 mat = new GrayF32(image.getWidth(), image.getHeight());
    mat.setData(luminance);
    GrayF32 output = new GrayF32(image.getWidth(), image.getHeight());
    BlurImageOps.mean(mat, output, 7, null);
    new FDistort(output, resizedImg).scaleExt().apply();
    //This is real ugly but FDistort doesn't support GrayF64 mats
    for (int x = 0; x < N; x++) {
      for (int y = 0; y < N; y++) {
        resizedMat.set(x, y, resizedImg.get(x, y));
      }
    }
    CommonOps_DDRM.mult(DCT_MATRIX, resizedMat, intermediateMat);
    CommonOps_DDRM.mult(intermediateMat, DCT_MATRIX_TRANSPOSED, outputMat);
    //Zahner & Coskun/Sankur leave out (0,y) and (x,0) since it doesn't contain relevant info
    double[] relevant = new double[8 * 8];
    for (int x = 1; x <= 8; x++) {
      for (int y = 1; y <= 8; y++) {
        relevant[(y - 1) * 8 + (x - 1)] = outputMat.get(x, y);
      }
    }
    double median = new Median().evaluate(relevant);
    BitSet feature = new BitSet(64);
    for (int i = 0; i < 64; i++) {
      feature.set(i, relevant[i] > median);
    }
    LOGGER.info("Processed in {} ms", (System.nanoTime() - start) / 1_000_000);
    return feature;
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    final MultiImage image = shot.getMostRepresentativeFrame().getImage();
    final BitSet feature = extractHash(image);
    if (shot.getId() == null) {
      throw new RuntimeException(shot.getId() + "" + feature);
    }
    this.writer.write(Pair.of(shot.getId(), feature));
  }

  @Override
  public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
    this.phandler = phandlerSupply.get();
    this.writer = new SimpleBitSetWriter(this.phandler, batchSize, this.tableName);
  }

  @Override
  public void finish() {
    LOGGER.debug("Finishing");
    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }
    super.finish();
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    return this.getSimilar(new BitSetTypeProvider(extractHash(sc.getMostRepresentativeFrame().getImage())), qc);
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createEntity(this.tableName,
          new AttributeDefinition(GENERIC_ID_COLUMN_QUALIFIER, AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition(FEATURE_COLUMN_QUALIFIER, AttributeType.BITSET, 64)
    );
  }
}
