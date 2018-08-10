package org.vitrivr.cineast.core.features;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.StagedFeatureModule;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.audio.HPCP;
import org.vitrivr.cineast.core.util.dsp.fft.FFTUtil;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.BlackmanHarrisWindow;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;

/**
 * An Extraction and Retrieval module that leverages pure HPCP based CENS shingles according to [1]. These shingles can be used
 * for cover-song and version identification.
 *
 * [1] Grosche, P., & Muller, M. (2012). Toward characteristic audio shingles for efficient cross-version music retrieval.
 *      In 2012 IEEE International Conference on Acoustics, Speech and Signal Processing (ICASSP) (pp. 473–476). IEEE. http://doi.org/10.1109/ICASSP.2012.6287919
 *
 * [2] Kurth, F., & Müler, M. (2008). Efficient index-based audio matching.
 *      IEEE Transactions on Audio, Speech and Language Processing, 16(2), 382–395. http://doi.org/10.1109/TASL.2007.911552
 *
 * @author rgasser
 * @version 1.0
 * @created 16.02.17
 */
public abstract class CENS extends StagedFeatureModule {

    /** Size of the window during STFT in seconds (as proposed in [2]). */
    private final static float WINDOW_SIZE = 0.1f;

    /**
     * Size of a shingle in number of HPCP features.
     *
     * The final feature vector has SHINGLE_SIZE * HPCP.Resolution.Bins components.
     */
    private final static int SHINGLE_SIZE = 10;

    /**
     * Array of CENS(w,d) settings used at query time. One CENS feature is calculated and looked-up for every entry
     * in this array. The inner array contains the following parameters:
     *
     * 0 - Window size (w)
     * 1 - Downsampling ratio (d)
     */
    private final static int[][] QUERY_SETTINGS = {{11,2}, {21,5}, {41,10}, {81,20}};

    /** Maximum resolution to consider in HPCP calculation. */
    private final float minFrequency;

    /** Minimum resolution to consider in HPCP calculation. */
    private final float maxFrequency;

    /**
     * Default constructor.
     *
     * @param tableName Name of the entity (for persistence writer).
     *
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
     * This method represents the first step that's executed when processing query. The associated SegmentContainer is
     * examined and feature-vectors are being generated. The generated vectors are returned by this method together with an
     * optional weight-vector.
     *
     * <strong>Important: </strong> The weight-vector must have the same size as the feature-vectors returned by the method.
     *
     * @param sc SegmentContainer that was submitted to the feature module.
     * @param qc A QueryConfig object that contains query-related configuration parameters. Can still be edited.
     * @return List of feature vectors for lookup.
     */
    @Override
    protected List<float[]> preprocessQuery(SegmentContainer sc, ReadableQueryConfig qc) {

        /* Create STFT. If this fails, return empty list. */
        final Pair<Integer,Integer> parameters = FFTUtil.parametersForDuration(sc.getSamplingrate(), WINDOW_SIZE);
        final STFT stft = sc.getSTFT(parameters.first, (parameters.first-2*parameters.second)/3 ,parameters.second, new BlackmanHarrisWindow());
        if (stft == null) {
            return new ArrayList<>(0);
        }

         /* Prepare empty features. */
        final List<float[]> features = new ArrayList<>(3 * QUERY_SETTINGS.length);

        /* Prepare HPCPs... */
        final HPCP hpcps = new HPCP(HPCP.Resolution.FULLSEMITONE, minFrequency, maxFrequency);
        hpcps.addContribution(stft);

        /*
         * ... and derive CENS features; only three features per QUERY_SETTING are kept because the shifts
         * resulting from the Shingeling are realised as individual vectors during ingest!
         */
        for (int[] QUERY_SETTING : QUERY_SETTINGS) {
            List<float[]> cens = this.getFeatures(hpcps, QUERY_SETTING[0], QUERY_SETTING[1]);
            for (int i=0; i<cens.size(); i++) {
                if (i % SHINGLE_SIZE == 0) {
                  features.add(cens.get(i));
                }
            }
        }

        return features;
    }

    /**
     * This method represents the last step that's executed when processing a query. A list of partial-results (DistanceElements) returned by
     * the lookup stage is processed based on some internal method and finally converted to a list of ScoreElements. The filtered list of
     * ScoreElements is returned by the feature module during retrieval.
     *
     * @param partialResults List of partial results returned by the lookup stage.
     * @param qc A ReadableQueryConfig object that contains query-related configuration parameters.
     * @return List of final results. Is supposed to be de-duplicated and the number of items should not exceed the number of items per module.
     */
    @Override
    protected List<ScoreElement> postprocessQuery(List<SegmentDistanceElement> partialResults, ReadableQueryConfig qc) {
        /* Prepare map to build a unique set of results. */
        final HashMap<String,DistanceElement> map = new HashMap<>();
        for (DistanceElement hit : partialResults) {
            map.merge(hit.getId(), hit, (o, n) -> o.getDistance() > n.getDistance() ? n : o);
        }

        /* Prepare final list of results. */
        final CorrespondenceFunction correspondence = qc.getCorrespondenceFunction().orElse(this.correspondence);
        return ScoreElement.filterMaximumScores(map.entrySet().stream().map((e) -> e.getValue().toScore(correspondence)));
    }

    /**
     * Processes a SegmentContainer for later persisting it in the storage layer.
     *
     * @param sc The SegmentContainer that should be processed.
     */
    @Override
    public void processSegment(SegmentContainer sc) {
        /* Create STFT. If this fails, return empty list. */
        Pair<Integer,Integer> parameters = FFTUtil.parametersForDuration(sc.getSamplingrate(), WINDOW_SIZE);
        STFT stft = sc.getSTFT(parameters.first, (parameters.first-2*parameters.second)/3, parameters.second, new HanningWindow());
        if (stft == null) {
          return;
        }

        /* Prepare HPCPs... */
        HPCP hpcps = new HPCP(HPCP.Resolution.FULLSEMITONE, minFrequency, maxFrequency);
        hpcps.addContribution(stft);

        List<float[]> features = this.getFeatures(hpcps, 41, 10);
        features.forEach(f -> this.persist(sc.getId(), new FloatVectorImpl(f)));
    }

    /**
     * Merges the provided QueryConfig with the default QueryConfig enforced by the
     * feature module.
     *
     * @param qc QueryConfig provided by the caller of the feature module.
     * @return Modified QueryConfig.
     */
    @Override
    protected QueryConfig defaultQueryConfig(ReadableQueryConfig qc) {
        return new QueryConfig(qc)
                .setCorrespondenceFunctionIfEmpty(this.correspondence)
                .setDistanceIfEmpty(QueryConfig.Distance.euclidean)
                .addHint(ReadableQueryConfig.Hints.inexact);
    }

    /**
     * Returns a list of CENS shingle feature vectors given a SegmentContainer.
     *
     * @param hpcps HPCP from which to derive the feature vectors.
     * @param w Window size w for CENS(w,d)
     * @param downsample Downsample ratio d for CENS(w,d)
     * @return List of CENS Shingle feature vectors.
     */
    private List<float[]> getFeatures(HPCP hpcps, int w, int downsample) {
        /* Prepare empty list of results. */
        List<float[]> features = new ArrayList<>();

        double[][] cens = org.vitrivr.cineast.core.util.audio.CENS.cens(hpcps, w, downsample);
        int numberoffeatures = cens.length - SHINGLE_SIZE + 1;

        for (int i = 0; i < numberoffeatures; i++) {
            float[] feature = new float[SHINGLE_SIZE * HPCP.Resolution.FULLSEMITONE.bins];
            for (int j = 0; j < SHINGLE_SIZE; j++) {
                for (int k = 0; k < HPCP.Resolution.FULLSEMITONE.bins; k++) {
                   feature[j*HPCP.Resolution.FULLSEMITONE.bins + k] =  (float)cens[j + i][k];
                }
            }
            if (MathHelper.checkNotZero(feature) && MathHelper.checkNotNaN(feature)) {
                features.add(MathHelper.normalizeL2(feature));
            }
        }

        return features;
    }
}
