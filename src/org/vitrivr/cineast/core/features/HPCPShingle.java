package org.vitrivr.cineast.core.features;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.*;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.audio.HPCP;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.BlackmanHarrisWindow;

import java.util.*;
import java.util.stream.Collectors;

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
    private final static int WINDOW_SIZE = 8192;

    /** Overlap between two subsequent frames during STFT in samples. */
    private final static int WINDOW_OVERLAP = 2205;

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
        /* Get list of features. */
        List<float[]> features = this.getFeatures(sc);

        /* Distance is always Cosine-Distance. */
        qc.setDistance(QueryConfig.Distance.cosine);

        /* Prepare helper data-structures. */
        final List<StringDoublePair> results = new ArrayList<>();
        final TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();

        for (float[] feature : features) {
            List<StringDoublePair> partial = this.selector.getNearestNeighbours(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), feature, "feature", qc);
            for (StringDoublePair hit : partial) {
                if (map.containsKey(hit.key)) {
                    double current = map.get(hit.key);
                    map.adjustValue(hit.key, (hit.value-current)/2.0f);
                } else {
                    map.put(hit.key, hit.value);
                }
            }
        }

        /* Prepare final result-set. */
        map.forEachEntry((key, value) -> results.add(new StringDoublePair(key, MathHelper.getScore(value, this.maxDist))));
        return results;
    }

    /**
     * Processes a SegmentContainer during extraction; calculates the HPCP for the container and
     * creates and persists a set shingle-vectors.
     *
     * @param segment SegmentContainer to process.
     */
    public void processShot(SegmentContainer segment) {
        List<float[]> list = this.getFeatures(segment);
        list.forEach(f -> this.persist(segment.getId(), new FloatVectorImpl(f)));
    }

    /**
     * Returns a list of feature vectors given a SegmentContainer.
     *
     * @param segment SegmentContainer for which to calculate the feature vectors.
     * @return List of HPCP Shingle feature vectors.
     */
    private List<float[]> getFeatures(SegmentContainer segment) {
        /* Create STFT. IF this fails, return empty list. */
        STFT stft = segment.getSTFT(WINDOW_SIZE, WINDOW_OVERLAP, new BlackmanHarrisWindow());
        if (stft == null) return new ArrayList<>();

        HPCP hpcps = new HPCP(this.resolution, this.min_frequency, this.max_frequency);
        hpcps.addContribution(stft);

        int vectors = Math.max(hpcps.size() - SHINGLE_SIZE, 1);
        double powers = 1.0f;

        List<Pair<Double, float[]>> features = new ArrayList<>(vectors);
        for (int n = 0; n < vectors; n++) {
            Pair<Double, float[]> feature = this.getHPCPShingle(hpcps, n);
            features.add(feature);
            powers *= feature.first;
        }

        final double threshold = (Math.pow(powers, 1.0/features.size()) / 4.0);
        return features.stream().filter(f -> (f.first > threshold)).map(f -> f.second).collect(Collectors.toList());
    }


    /**
     * Returns a constant length HPCP shingle from a Harmonic Pitch Class Profile. If the
     * resulting shingle is a zero-vector, this method will return null instead.
     *
     * @param hpcps The HPCP to create the shingle from.
     * @param shift The index to shift the HPCP's by.
     * @return HPCP shingle (feature vector) or null.
     */
    private Pair<Double, float[]> getHPCPShingle(HPCP hpcps, int shift) {
        float[] feature = new float[SHINGLE_SIZE * this.resolution.bins];
        for (int i = shift; i < SHINGLE_SIZE + shift; i++) {
            if (i < hpcps.size()) {
                float[] hpcp = hpcps.getHpcp(i);
                System.arraycopy(hpcp, 0, feature, (i - shift) * this.resolution.bins, hpcp.length);
            }
        }
        return new Pair<>(MathHelper.normL2(feature), MathHelper.normalizeL2(feature));
    }
}
