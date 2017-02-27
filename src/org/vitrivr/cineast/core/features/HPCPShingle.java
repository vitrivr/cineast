package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.Config;
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
import java.util.Map;

/**
 * An Extraction and Retrieval module that leverages pure HPCP shingles according to [1]. These shingles can be used
 * for cover-song and version identification.
 *
 * [1] Casey, M., Rhodes, C., & Slaney, M. (2008). Analysis of minimum distances in high-dimensional musical spaces.
 *  IEEE Transactions on Audio, Speech and Language Processing, 16(5), 1015–1028. http://doi.org/10.1109/TASL.2008.925883
 *
 * @author rgasser
 * @version 1.0
 * @created 26.02.17
 */
public abstract class HPCPShingle extends AbstractFeatureModule {

    /** Size of the window during STFT in samples. */
    private final static int WINDOW_SIZE = 4096;

    /** Overlap between two subsequent frames during STFT in samples. */
    private final static int WINDOW_OVERLAP = 1024;

    /** Size of a shingle in number of HPCP features.
     *
     * The final feature vector has SHINGLE_SIZE * HPCP.Resolution.Bins components
     */
    private final static int SHINGLE_SIZE = 30;

    /** Minimum resolution to consider in HPCP calculation. */
    private final float min_frequency;

    /** Maximum resolution to consider in HPCP calculation. */
    private final float max_frequency;

    /** HPCP resolution (12, 24, 36 bins). */
    private final HPCP.Resolution resolution;

    /** The maximum distance at which partial results are still counted towards the final result. */
    private final float threshold;

    /**
     * Default constructor.
     *
     * @param name Name of the entity (for persistence writer).
     * @param min_frequency Minimum frequency to consider during HPCP analysis.
     * @param max_frequency Maximum frequency to consider during HPCP analysis.
     * @param resolution Resolution of HPCP (i.e. number of HPCP bins).
     */
    protected HPCPShingle(String name, float min_frequency, float max_frequency, HPCP.Resolution resolution) {
        super(name, 2.0f);

        /* Assign variables. */
        this.min_frequency = min_frequency;
        this.max_frequency = max_frequency;
        this.resolution = resolution;
        this.threshold = 3.0f*this.maxDist/4.0f;
    }

    /**
     * Processes a SegmentContainer to find entries that are similar to the provided container.
     *
     * @param sc
     * @param qc
     * @return
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {

        STFT stft = sc.getSTFT(WINDOW_SIZE, WINDOW_OVERLAP, new BlackmanHarrisWindow());
        HPCP hpcps = new HPCP(this.resolution, this.min_frequency, this.max_frequency);
        hpcps.addContribution(stft);

        /* Distance is always Cosine-Distance. */
        qc.setDistance(QueryConfig.Distance.cosine);

        /* Get list of HPCP vectors sorted by distance from mean HPCP. */
        List<StringDoublePair> results = new ArrayList<>();
        HashMap<String, Double> map = new HashMap<>();
        int vectors = hpcps.size() - SHINGLE_SIZE;
        double max = 0.0;
        for (int n = 0; n < vectors; n++) {
            float[] feature = this.getHPCPShingle(hpcps, n);
            if (feature != null) {
                List<StringDoublePair> partial = this.selector.getNearestNeighbours(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), MathHelper.normalizeL2(feature), "feature", qc);
                for (StringDoublePair hit : partial) {
                    double distance = hit.value;
                    if (distance < threshold) {
                        if (map.containsKey(hit.key)) {
                            map.put(hit.key, map.get(hit.key) + 1.0);
                        } else {
                            map.put(hit.key, 1.0);
                        }
                        max = Math.max(map.get(hit.key), max);
                    }
                }
            }
        }

        /* Prepare final result-set. */
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            results.add(new StringDoublePair(entry.getKey(), entry.getValue() / max));
        }
        return results;
    }

    /**
     * Processes a SegmentContainer during extraction; calculates the HPCP for the container and
     * creates and persists a set shingle-vectors.
     *
     * @param segment SegmentContainer to process.
     */
    public void processShot(SegmentContainer segment) {
        STFT stft = segment.getSTFT(WINDOW_SIZE, WINDOW_OVERLAP, new BlackmanHarrisWindow());
        HPCP hpcps = new HPCP(this.resolution, this.min_frequency, this.max_frequency);
        hpcps.addContribution(stft);

        List<ReadableFloatVector> list = new ArrayList<>();

        int vectors = hpcps.size() - SHINGLE_SIZE;
        for (int n = 0; n < vectors; n++) {
            float[] feature = this.getHPCPShingle(hpcps, n);
            if (feature != null) {
                list.add(new FloatVectorImpl(MathHelper.normalizeL2(feature)));
            }
        }

        /* Persist most representative HPCP vector. */
        this.persist(segment.getId(), list);
    }


    /**
     * Returns a constant length HPCP shingle from a Harmonic Pitch Class Profile. If the
     * resulting shingle is a zero-vector, this method will return null instead.
     *
     * @param hpcps The HPCP to create the shingle from.
     * @param shift The index to shift the HPCP's by.
     * @return HPCP shingle (feature vector) or null.
     */
    private float[] getHPCPShingle(HPCP hpcps, int shift) {
        float[] feature = new float[SHINGLE_SIZE * this.resolution.bins];

        for (int i = shift; i < SHINGLE_SIZE; i++) {
            float[] hpcp =  hpcps.getHpcp(i);
            System.arraycopy(hpcp, 0, feature, (i-shift) * this.resolution.bins, hpcp.length);
        }

        if (MathHelper.checkNotZero(feature)) {
            return MathHelper.normalizeL2(feature);
        } else {
            return null;
        }
    }
}
