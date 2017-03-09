package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.*;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.filter.audio.PreEmphasisFilter;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.audio.MFCC;
import org.vitrivr.cineast.core.util.fft.STFT;
import org.vitrivr.cineast.core.util.fft.windows.HanningWindow;
import org.vitrivr.cineast.core.util.fft.windows.RectangularWindow;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rgasser
 * @version 1.0
 * @created 28.02.17
 */
public class MFCCShingle extends AbstractFeatureModule {

    /** Size of the window during STFT in # samples. */
    private final static int WINDOW_SIZE = 8192;

    /** Overlap between two subsequent frames during STFT in # samples. */
    private final static int WINDOW_OVERLAP = 2205;

    /** */
    private final static int SHINGLE_SIZE = 30;

    /** */
    private final float threshold;

    /**
     *
     */
    public MFCCShingle() {
        super("features_mfccshingles", 2.0f);
        this.threshold = 2.0f*this.maxDist/4.0f;
    }

    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        /* Extract MFCC shingle features from QueryObject. */
        List<float[]> features = this.getFeatures(sc);

        /* Prepare helper data-structures. */
        final List<StringDoublePair> results = new ArrayList<>();
        final HashMap<String, Integer> map = new HashMap<>();
        final HashSet<String> seen = new HashSet<>(250);

        qc.setDistance(QueryConfig.Distance.euclidean);

        int stepsize = Math.max((int)Math.floor(features.size()/10), 1);

        for (int i = 0; i<features.size()-stepsize;i+=stepsize) {
            List<StringDoublePair> partial = this.selector.getNearestNeighbours(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), features.get(i), "feature", qc);
            seen.clear();
            for (StringDoublePair hit : partial) {
                if (hit.value > this.threshold) break;
                if (!seen.contains(hit.key)) {
                    if (!map.containsKey(hit.key)) map.put(hit.key, 0);
                    map.put(hit.key, map.get(hit.key) + 1);
                    seen.add(hit.key);
                }
            }
        }

        /* Prepare final result-set. */
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            results.add(new StringDoublePair(entry.getKey(), (double) entry.getValue() / (double) features.size()));
        }

        return results;
    }

    /**
     * 
     * @param sc
     */
    @Override
    public void processShot(SegmentContainer sc) {
        List<float[]> features = this.getFeatures(sc);
        features.forEach(f -> this.persist(sc.getId(), new FloatVectorImpl(f)));
    }

    /**
     * Derives and returns a list of MFCC features for a SegmentContainer.
     *
     * @param segment SegmentContainer to derive the MFCC features from.
     * @return List of MFCC shingles.
     */
    private List<float[]> getFeatures(SegmentContainer segment) {
        STFT stft = segment.getSTFT(WINDOW_SIZE, WINDOW_OVERLAP, new HanningWindow());
        List<MFCC> mfccs = MFCC.calculate(stft);
        int vectors = mfccs.size() - SHINGLE_SIZE;

        List<float[]> features = new ArrayList<>(Math.max(1, vectors));
        if (vectors > 0) {
            for (int i = 0; i < vectors; i++) {
                float[] feature = new float[SHINGLE_SIZE * 13];
                for (int j = 0; j < SHINGLE_SIZE; j++) {
                    MFCC mfcc = mfccs.get(i + j);
                    System.arraycopy(mfcc.getCepstra(), 0, feature, 13 * j, 13);
                }
                features.add(MathHelper.normalizeL2(feature));
            }
        }
        return features;
    }
}
