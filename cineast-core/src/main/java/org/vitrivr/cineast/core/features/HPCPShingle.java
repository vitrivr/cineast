package org.vitrivr.cineast.core.features;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An Extraction and Retrieval module that leverages pure HPCP shingles according to [1]. These shingles can be used
 * for cover-song and version identification.
 *
 * [1] Casey, M., Rhodes, C., & Slaney, M. (2008). Analysis of minimum distances in high-dimensional musical spaces.
 *  IEEE Transactions on Audio, Speech and Language Processing, 16(5), 1015–1028. http://doi.org/10.1109/TASL.2008.925883
 *
 */
public abstract class HPCPShingle extends StagedFeatureModule {

    /** Duration of the window during STFT in seconds. */
    private final static float WINDOW_SIZE = 0.200f;

    /**
     * Size of a shingle in number of HPCP features.
     *
     * The final feature vector has SHINGLE_SIZE * HPCP.Resolution.Bins components
     */
    private final static int SHINGLE_SIZE = 25;

    /** Minimum resolution to consider in HPCP calculation. */
    private final float min_frequency;

    /** Maximum resolution to consider in HPCP calculation. */
    private final float max_frequency;

    /** HPCP resolution (12, 24, 36 bins). */
    private final HPCP.Resolution resolution;

    /** Distance-threshold used to sort out vectors that should should not count in the final scoring stage. */
    private final float distanceThreshold;

    /**
     * Default constructor.
     *
     * @param name Name of the entity (for persistence writer).
     * @param min_frequency Minimum frequency to consider during HPCP analysis.
     * @param max_frequency Maximum frequency to consider during HPCP analysis.
     * @param resolution Resolution of HPCP (i.e. number of HPCP bins).
     */
    protected HPCPShingle(String name, float min_frequency, float max_frequency, HPCP.Resolution resolution) {
        super(name, 2.0f, SHINGLE_SIZE * resolution.bins);

        /* Assign variables. */
        this.min_frequency = min_frequency;
        this.max_frequency = max_frequency;
        this.resolution = resolution;
        this.distanceThreshold = 0.9f;
    }

    /**
     * This method represents the first step that's executed when processing query. The associated SegmentContainer is
     * examined and feature-vectors are being generated. The generated vectors are returned by this method together with an
     * optional weight-vector.
     * <p>
     * <strong>Important: </strong> The weight-vector must have the same size as the feature-vectors returned by the method.
     *
     * @param sc SegmentContainer that was submitted to the feature module.
     * @param qc A QueryConfig object that contains query-related configuration parameters. Can still be edited.
     * @return A pair containing a List of features and an optional weight vector.
     */
    @Override
    protected List<float[]> preprocessQuery(SegmentContainer sc, ReadableQueryConfig qc) {
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
        final HashMap<String,DistanceElement> map = new HashMap<>();
        for (DistanceElement hit : partialResults) {
            if (hit.getDistance() <= this.distanceThreshold) {
                map.merge(hit.getId(), hit, (o, n) -> new SegmentDistanceElement(o.getId(), (o.getDistance() + n.getDistance())/2));
            }
        }

        /* Prepare final result-set. */
        final CorrespondenceFunction fkt = qc.getCorrespondenceFunction().orElse(this.correspondence);
        return ScoreElement.filterMaximumScores(map.entrySet().stream().map(e -> e.getValue().toScore(fkt)));
    }

    /**
     * Processes a SegmentContainer during extraction; calculates the HPCP for the container and
     * creates and persists a set shingle-vectors.
     *
     * @param segment SegmentContainer to process.
     */
    @Override
    public void processSegment(SegmentContainer segment) {
        final List<float[]> features = this.getFeatures(segment);

        /*
         * Persists only the individual, disjoint shingle vectors
         */
        features.stream()
                .filter((feature) -> features.indexOf(feature) % SHINGLE_SIZE == 0)
                .forEach((feature) -> this.persist(segment.getId(), new FloatVectorImpl(feature)));
    }

    /**
     * Returns a modified QueryConfig for the given feature. This implementation copies the original configuaration and
     * sets a weight-vector, which depends on the feature vector.
     *
     * @param qc Original query config
     * @param feature Feature for which a weight-vector is required.
     * @return New query config.
     */
    protected ReadableQueryConfig queryConfigForFeature(QueryConfig qc, float[] feature) {
        float[] weight = new float[feature.length];
        for (int i=0;i<feature.length;i++) {
            if (feature[i] == 0.0f) {
                weight[i] = 10.0f;
            }
        }
        return qc.clone().setDistanceWeights(weight);
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
     * Returns a list of feature vectors given a SegmentContainer.
     *
     * @param segment SegmentContainer for which to calculate the feature vectors.
     * @return List of HPCP Shingle feature vectors.
     */
    private List<float[]> getFeatures(SegmentContainer segment) {
        /* Create STFT; If this fails, return empty list. */
        Pair<Integer,Integer> parameters = FFTUtil.parametersForDuration(segment.getSamplingrate(), WINDOW_SIZE);
        STFT stft = segment.getSTFT(parameters.first,(parameters.first - 2*parameters.second)/2, parameters.second, new HanningWindow());
        if (stft == null) {
            return new ArrayList<>(0);
        }

        HPCP hpcps = new HPCP(this.resolution, this.min_frequency, this.max_frequency);
        hpcps.addContribution(stft);

        int vectors = Math.max(hpcps.size() - SHINGLE_SIZE, 1);
        final SummaryStatistics statistics = new SummaryStatistics();

        List<Pair<Double, float[]>> features = new ArrayList<>(vectors);
        for (int n = 0; n < vectors; n++) {
            Pair<Double, float[]> feature = this.getHPCPShingle(hpcps, n);
            features.add(feature);
            statistics.addValue(feature.first);
        }

        final double threshold = 0.25*statistics.getGeometricMean();
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
