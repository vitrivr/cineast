package org.vitrivr.cineast.core.features;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.*;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.audio.MFCC;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;

import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 28.02.17
 */
public class MFCCShingle extends AbstractFeatureModule {

    /** Size of the window during STFT in # samples. */
    private final static int WINDOW_SIZE = 4096;

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
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        /* Extract MFCC shingle features from QueryObject. */
        List<float[]> features = this.getFeatures(sc);

        /* Prepare helper data-structures. */
        final List<ScoreElement> results = new ArrayList<>();
        final TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();
        final HashSet<String> seen = new HashSet<>(Config.sharedConfig().getRetriever().getMaxResultsPerModule());

        /* Determine, how many lookups should be performed. */
        final int maxlookup = 5;
        final int stepsize = Math.max((int)Math.floor(features.size()/maxlookup), 1);
        final double maxDist = ((features.size()/stepsize) * this.maxDist);

       /* Set QueryConfig and extract correspondence function. */
        qc = this.setQueryConfig(qc);
        final CorrespondenceFunction correspondence = qc.getCorrespondenceFunction().orElse(this.linearCorrespondence);

        for (int i = 0; i<features.size()-stepsize;i+=stepsize) {
            List<SegmentDistanceElement> partial = this.selector.getNearestNeighbours(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), features.get(i), "feature", SegmentDistanceElement.class, qc);
            seen.clear();
            for (SegmentDistanceElement hit : partial) {
                if (hit.getDistance() > this.threshold) break;
                if (!seen.contains(hit.getSegmentId())) {
                    map.adjustOrPutValue(hit.getSegmentId(), this.maxDist - hit.getDistance(), 0.0);
                    seen.add(hit.getSegmentId());
                }
            }
        }

        /* Prepare final result-set. */
        map.forEachEntry((key, value) -> results.add(new SegmentScoreElement(key, correspondence.applyAsDouble(value))));
        ScoreElement.filterMaximumScores(results.stream());
        return results;
    }

    /**
     * Merges the provided QueryConfig with the default QueryConfig enforced by the
     * feature module.
     *
     * @param qc QueryConfig provided by the caller of the feature module.
     * @return Modified QueryConfig.
     */
    protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
        return new QueryConfig(qc)
                .setCorrespondenceFunctionIfEmpty(this.linearCorrespondence)
                .setDistanceIfEmpty(QueryConfig.Distance.euclidean);
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
