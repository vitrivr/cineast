package org.vitrivr.cineast.core.features;


import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.score.ScoreElements;
import org.vitrivr.cineast.core.data.score.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.audio.HPCP;

import org.vitrivr.cineast.core.util.fft.STFT;
import org.vitrivr.cineast.core.util.fft.windows.BlackmanHarrisWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * An Extraction and Retrieval module that leverages pure HPCP based CENS shingles according to [1].
 * These shingles can be used for cover-song and version identification.
 *
 * [1] Grosche, P., & Muller, M. (2012). Toward characteristic audio shingles for efficient cross-version music retrieval.
 *    In 2012 IEEE International Conference on Acoustics, Speech and Signal Processing (ICASSP) (pp. 473–476). IEEE. http://doi.org/10.1109/ICASSP.2012.6287919

 * @author rgasser
 * @version 1.0
 * @created 16.02.17
 */
public abstract class CENS extends AbstractFeatureModule {

  /** Size of the window during STFT in # samples. */
  private static final int WINDOW_SIZE = 4096;

  /** Overlap between two subsequent frames during STFT in # samples. */
  private static final int WINDOW_OVERLAP = 1024;

  /**
   * Size of a shingle in number of HPCP features.
   *
   * The final feature vector has SHINGLE_SIZE * HPCP.Resolution.Bins components.
   */
  private static final int SHINGLE_SIZE = 10;

  /**
   * Array of CENS(w,d) settings used at query time. One CENS feature is calculated and looked-up for every entry
   * in this array. The inner array contains the following parameters:
   *
   * 0 - Window size (w)
   * 1 - Downsamling ratio (d)
   * 2 - Weight
   */
  private static final int[][] querySettings = {
      { 5,  1, 1},
      {11,  2, 2},
      {21,  5, 4},
      {41, 10, 2},
      {81, 20, 1}
  };

  /** Maximum resolution to consider in HPCP calculation. */
  private final float minFrequency;

  /** Minimum resolution to consider in HPCP calculation. */
  private final float maxFrequency;

  /**
   * Default constructor.
   *
   * @param tableName Name of the entity (for persistence writer).
   * @param minFrequency Minimum frequency to consider during HPCP analysis.
   * @param maxFrequency Maximum frequency to consider during HPCP analysis.
   */
  public CENS(String tableName, float minFrequency, float maxFrequency) {
    super(tableName, 2.0f);

    /* Apply fields. */
    this.minFrequency = minFrequency;
    this.maxFrequency = maxFrequency;
  }

  /**
   *
   * @param sc
   * @param rqc
   * @return
   */
  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig rqc) {

    STFT stft = sc.getSTFT(WINDOW_SIZE, WINDOW_OVERLAP, new BlackmanHarrisWindow());
    HPCP hpcps = new HPCP(HPCP.Resolution.FULLSEMITONE, this.minFrequency, this.maxFrequency);
    hpcps.addContribution(stft);

    ReadableQueryConfig qc = setQueryConfig(rqc);

    TObjectDoubleMap<String> map = new TObjectDoubleHashMap<>();
    double max = 0.0;
    for (int[] querySetting : querySettings) {
      List<float[]> features = this.getFeatures(sc, querySetting[0], querySetting[1]);
      if (features.size() > 0) {
        float[] feature = features.get(0);
        List<SegmentDistanceElement> partial = this.selector
            .getNearestNeighbours(250, feature, "feature", SegmentDistanceElement.class, qc);
        for (SegmentDistanceElement hit : partial) {
          String segmentId = hit.getSegmentId();
          double value = querySetting[2] * (this.maxDist - hit.getDistance());
          double putValue = map.adjustOrPutValue(segmentId, value, value);
          max = Math.max(putValue, max);
        }
      }
    }

    final double finalMax = max;
    return ScoreElements.scoresFromSegmentsDistanceMap(map, CorrespondenceFunction.fromFunction(score -> score / finalMax));
  }

  /**
   *
   * @param shot
   */
  @Override
  public void processShot(SegmentContainer shot) {
    List<float[]> features = this.getFeatures(shot, 21, 5);
    features.forEach(f -> this.persist(shot.getId(), new FloatVectorImpl(f)));
  }

  /**
   * Returns a list of CENS shingle feature vectors given a SegmentContainer.
   *
   * @param segment SegmentContainer for which to calculate the feature vectors.
   * @param w Window size w for CENS(w,d)
   * @param downsample Downsample ratio d for CENS(w,d)
   * @return List of CENS Shingle feature vectors.
   */
  private List<float[]> getFeatures(SegmentContainer segment, int w, int downsample) {
    STFT stft = segment.getSTFT(WINDOW_SIZE, WINDOW_OVERLAP, new BlackmanHarrisWindow());
    HPCP hpcps = new HPCP(HPCP.Resolution.FULLSEMITONE, minFrequency, maxFrequency);
    hpcps.addContribution(stft);

    double[][] cens = org.vitrivr.cineast.core.util.audio.CENS.cens(hpcps, w, downsample);
    int numberoffeatures = cens.length - SHINGLE_SIZE + 1;

    List<float[]> features = new ArrayList<>();

    for (int i = 0; i < numberoffeatures; i++) {
      float[] feature = new float[SHINGLE_SIZE * HPCP.Resolution.FULLSEMITONE.bins];
      for (int j = 0; j < SHINGLE_SIZE; j++) {
        for (int k = 0; k < HPCP.Resolution.FULLSEMITONE.bins; k++) {
          feature[j * HPCP.Resolution.FULLSEMITONE.bins + k] = (float) cens[j + i][k];
        }
      }
      if (MathHelper.checkNotZero(feature)) {
        features.add(MathHelper.normalizeL2(feature));
      }
    }

    return features;
  }
  
  @Override
  protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
    return new QueryConfig(qc).setDistanceIfEmpty(QueryConfig.Distance.cosine);
  }
}
