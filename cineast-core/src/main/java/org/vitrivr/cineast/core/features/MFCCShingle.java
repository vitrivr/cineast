package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.StagedFeatureModule;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.audio.MFCC;
import org.vitrivr.cineast.core.util.dsp.fft.FFTUtil;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;

import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author rgasser
 * @version 1.0
 * @created 28.02.17
 */
public class MFCCShingle extends StagedFeatureModule {

    /** Size of the window during STFT in # samples. */
    private final static float WINDOW_SIZE = 0.2f;

    /** Size of a single MFCC Shingle. */
    private final static int SHINGLE_SIZE = 25;

    /** Distance-threshold used to sort out vectors that should should not count in the final scoring stage. */
    private final float distanceThreshold;

    /**
     *
     */
    public MFCCShingle() {
        super("features_mfccshingles", 2.0f, SHINGLE_SIZE * 13);
        this.distanceThreshold = 0.1f;
    }

    /**
     * This method represents the first step that's executed when processing query. The associated SegmentContainer is
     * examined and feature-vectors are being generated. The generated vectors are returned by this method together with an
     * optional weight-vector.
     * <p>
     * <strong>Important: </strong> The weight-vector must have the same size as the feature-vectors returned by the method.
     *
     * @param sc SegmentContainer that was submitted to the feature module
     * @param qc A QueryConfig object that contains query-related configuration parameters. Can still be edited.
     * @return A pair containing a List of features and an optional weight vector.
     */
    @Override
    protected List<float[]> preprocessQuery(SegmentContainer sc, ReadableQueryConfig qc) {
        /* Extract MFCC shingle features from QueryObject. */
        return this.getFeatures(sc).stream().limit(SHINGLE_SIZE * 2).collect(Collectors.toList());
    }

    /**
     * This method represents the last step that's executed when processing a query. A list of partial-results (DistanceElements) returned by
     * the lookup stage is processed based on some internal method and finally converted to a list of ScoreElements. The filtered list of
     * ScoreElements is returned by the feature module during retrieval.
     *
     * @param partialResults List of partial results returned by the lookup stage.
     * @param qc             A ReadableQueryConfig object that contains query-related configuration parameters.
     * @return List of final results. Is supposed to be de-duplicated and the number of items should not exceed the number of items per module.
     */
    @Override
    protected List<ScoreElement> postprocessQuery(List<SegmentDistanceElement> partialResults, ReadableQueryConfig qc) {
         /* Prepare helper data-structures. */
        final List<ScoreElement> results = new ArrayList<>();
        final TObjectIntHashMap<String> scoreMap = new TObjectIntHashMap<>();

         /* Set QueryConfig and extract correspondence function. */
        qc = this.setQueryConfig(qc);
        final CorrespondenceFunction correspondence = qc.getCorrespondenceFunction().orElse(this.correspondence);
        for (DistanceElement hit : partialResults) {
            if (hit.getDistance() < this.distanceThreshold) {
                scoreMap.adjustOrPutValue(hit.getId(), 1, scoreMap.get(hit.getId())/2);
            }
        }

        /* Prepare final result-set. */
        scoreMap.forEachEntry((key, value) -> results.add(new SegmentScoreElement(key, 1.0 - 1.0/value)));
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
    @Override
    protected QueryConfig defaultQueryConfig(ReadableQueryConfig qc) {
        return new QueryConfig(qc)
                .setCorrespondenceFunctionIfEmpty(this.correspondence)
                .setDistanceIfEmpty(QueryConfig.Distance.euclidean)
                .addHint(ReadableQueryConfig.Hints.inexact);
    }

    /**
     *
     * @param segment
     */
    @Override
    public void processSegment(SegmentContainer segment) {
        List<float[]> features = this.getFeatures(segment);

        /*
         * Persists only the individual, disjoint shingle vectors
         */
        features.stream()
                .filter((feature) -> features.indexOf(feature) % SHINGLE_SIZE == 0)
                .forEach((feature) -> this.persist(segment.getId(), new FloatVectorImpl(feature)));
    }

    /**
     * Derives and returns a list of MFCC features for a SegmentContainer.
     *
     * @param segment SegmentContainer to derive the MFCC features from.
     * @return List of MFCC Shingles.
     */
    private List<float[]> getFeatures(SegmentContainer segment) {
        final Pair<Integer,Integer> parameters = FFTUtil.parametersForDuration(segment.getSamplingrate(), WINDOW_SIZE);
        final STFT stft = segment.getSTFT(parameters.first, (parameters.first-2*parameters.second)/2, parameters.second, new HanningWindow());
        if (stft == null) {
            return new ArrayList<>(0);
        }
        final List<MFCC> mfccs = MFCC.calculate(stft);
        int vectors = mfccs.size() - SHINGLE_SIZE;

        List<float[]> features = new ArrayList<>(Math.max(1, vectors));
        if (vectors > 0) {
            for (int i = 0; i < vectors; i++) {
                float[] feature = new float[SHINGLE_SIZE * 13];
                for (int j = 0; j < SHINGLE_SIZE; j++) {
                    MFCC mfcc = mfccs.get(i + j);
                    System.arraycopy(mfcc.getCepstra(), 0, feature, 13 * j, 13);
                }
                if (MathHelper.checkNotZero(feature) && MathHelper.checkNotNaN(feature)) {
                    features.add(MathHelper.normalizeL2(feature));
                }
            }
        }
        return features;
    }
}
