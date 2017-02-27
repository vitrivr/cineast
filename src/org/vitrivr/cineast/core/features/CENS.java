package org.vitrivr.cineast.core.features;


import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.audio.HPCP;

import org.vitrivr.cineast.core.util.fft.STFT;
import org.vitrivr.cineast.core.util.fft.windows.BlackmanHarrisWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An Extraction and Retrieval module that leverages pure HPCP based CENS shingles according to [1]. These shingles can be used
 * for cover-song and version identification.
 *
 * [1] Grosche, P., & Muller, M. (2012). Toward characteristic audio shingles for efficient cross-version music retrieval.
 *      In 2012 IEEE International Conference on Acoustics, Speech and Signal Processing (ICASSP) (pp. 473–476). IEEE. http://doi.org/10.1109/ICASSP.2012.6287919

 * @author rgasser
 * @version 1.0
 * @created 16.02.17
 */
public abstract class CENS extends AbstractFeatureModule {


    /** Size of the window during STFT in # samples. */
    private final static int WINDOW_SIZE = 4096;

    /** Overlap between two subsequent frames during STFT in # samples. */
    private final static int WINDOW_OVERLAP = 1024;


    /** Size of a shingle in number of HPCP features.
     *
     * The final feature vector has SHINGLE_SIZE * HPCP.Resolution.Bins components.
     */
    private final static int SHINGLE_SIZE = 10;

    /** */
    private final static int[][] settings = {
        {5,1},
        {11,2},
        {21,5},
        {41,10},
        {81,20}
    };

    /** Maximum resolution to consider in HPCP calculation. */
    private final float minFrequency;

    /** Minimum resolution to consider in HPCP calculation. */
    private final float maxFrequency;

    /** */
    private final float threshold;

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
        this.threshold = this.maxDist/2.0f;
    }


    /**
     *
     * @param sc
     * @param qc
     * @return
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {

        STFT stft = sc.getSTFT(WINDOW_SIZE, WINDOW_OVERLAP, new BlackmanHarrisWindow());
        HPCP hpcps = new HPCP(HPCP.Resolution.FULLSEMITONE, this.minFrequency, this.maxFrequency);
        hpcps.addContribution(stft);

        qc.setDistance(QueryConfig.Distance.cosine);

        List<StringDoublePair> results = new ArrayList<>();

        HashMap<String, Double> map = new HashMap<>();
        float[] feature = new float[SHINGLE_SIZE * hpcps.getResolution().bins];
        double max = 0.0;
        for (int i=0; i<5; i++) {
            double[][] cens = org.vitrivr.cineast.core.util.audio.CENS.cens(hpcps, settings[i][0], settings[i][1]);
            for (int j = 0; j < SHINGLE_SIZE; j++) {
                for (int k = 0; k<hpcps.getResolution().bins; k++ ) {
                    if (j < cens.length) {
                        feature[j * hpcps.getResolution().bins + k] = (float) cens[j][k];
                    } else {
                        feature[j * hpcps.getResolution().bins + k] = 0.0f;
                    }
                }
            }

            List<StringDoublePair> partial = this.selector.getNearestNeighbours(250, MathHelper.normalizeL2(feature), "feature", qc);
            for (StringDoublePair hit : partial) {
                if (hit.value < this.threshold) {
                    if (map.containsKey(hit.key)) {
                        map.put(hit.key, map.get(hit.key) + (5.0-i));
                    } else {
                        map.put(hit.key, (5.0-i));
                    }
                    max = Math.max(map.get(hit.key), max);
                }
            }
        }

        final double realMax = max;
        map.forEach((key, value) -> {
            results.add(new StringDoublePair(key, value/realMax));
        });

        return results;
    }

    /**
     *
     * @param shot
     */
    @Override
    public void processShot(SegmentContainer shot) {
        STFT stft = shot.getSTFT(WINDOW_SIZE, WINDOW_OVERLAP, new BlackmanHarrisWindow());
        HPCP hpcps = new HPCP(HPCP.Resolution.FULLSEMITONE, minFrequency, maxFrequency);
        hpcps.addContribution(stft);

        double[][] cens = org.vitrivr.cineast.core.util.audio.CENS.cens(hpcps, 21, 5);

        if (cens.length >= SHINGLE_SIZE) {
            int features = cens.length - SHINGLE_SIZE + 1;
            List<ReadableFloatVector> vectors = new ArrayList<>();

            for (int i = 0; i < features; i++) {
                double[] feature = new double[SHINGLE_SIZE * HPCP.Resolution.FULLSEMITONE.bins];
                for (int j = 0; j < SHINGLE_SIZE; j++) {
                    System.arraycopy(cens[j + i], 0, feature,  j * HPCP.Resolution.FULLSEMITONE.bins, HPCP.Resolution.FULLSEMITONE.bins);
                }
                if (MathHelper.checkNotZero(feature)) {
                    vectors.add(new FloatVectorImpl(MathHelper.normalizeL2(feature)));
                }
            }

            this.persist(shot.getId(), vectors);
        }
    }
}
