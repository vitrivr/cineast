package org.vitrivr.cineast.core.features;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.*;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.*;

import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.abstracts.StagedFeatureModule;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.dsp.fft.FFTUtil;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;

import javax.swing.text.Segment;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.02.17
 */
public class AudioFingerprint extends StagedFeatureModule {
    /** Frequency-ranges that should be used to calculate the fingerprint. */
    private static final float[] RANGES = {20.0f, 40.0f, 80.0f, 120.0f, 180.0f, 300.0f, 420.0f};

    /** Length of an individual fingerprint (size of FV). */
    private static final int FINGERPRINT = 20 * (RANGES.length-1);

    /** Size of the window during STFT in seconds (as proposed in [2]). */
    private static final float WINDOW_SIZE = 0.2f;

    /**
     * Default constructor;
     */
    public AudioFingerprint() {
        super("features_audiofingerprint", 100.0f);
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
        /* Prepare empty list of features. */
        List<float[]> features = new ArrayList<>();

        /* Extract filtered spectrum and create query-vectors. */
        TIntArrayList filteredSpectrum = this.filterSpectrum(sc);
        int lookups = (filteredSpectrum.size() / FINGERPRINT + 1);


        for (int i=0;i<lookups;i++) {
            float[] feature = new float[FINGERPRINT];
            for (int j=0; j< FINGERPRINT; j++) {
                if (i * FINGERPRINT + j < filteredSpectrum.size()) {
                    feature[j] = filteredSpectrum.get(i * FINGERPRINT + j);
                }
            }
            features.add(feature);
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
    protected List<ScoreElement> postprocessQuery(List<DistanceElement> partialResults, ReadableQueryConfig qc) {
        /* Prepare empty list of results. */
        final ArrayList<ScoreElement> results = new ArrayList<>();
        final SummaryStatistics statistics = new SummaryStatistics();
        final HashMap<String,DistanceElement> map = new HashMap<>();

        /* Merge into map for final results; select the minimum distance. */
        for (DistanceElement result : partialResults) {
            statistics.addValue(result.getDistance());
            map.merge(result.getId(), result, (d1,d2) -> {
                if (d1.getDistance() > d2.getDistance()) {
                    return d2;
                } else {
                    return d1;
                }
            });
        }

        /* Return immediately if no partial results are available.  */
        if (map.isEmpty()) return results;

        /* Prepare final results. */
        final CorrespondenceFunction correspondence = CorrespondenceFunction.linear(statistics.getMean());
        map.forEach((key, value) -> results.add(value.toScore(correspondence)));
        return ScoreElement.filterMaximumScores(results.stream());
    }

    /**
     *
     * @param segment
     */
    @Override
    public void processShot(SegmentContainer segment) {
        TIntArrayList filteredSpectrum = this.filterSpectrum(segment);
        int shift = RANGES.length-1;
        int vectors = (filteredSpectrum.size() - FINGERPRINT) / shift;
        List<PersistentTuple> tuples = new ArrayList<>();
        for (int i = 0; i <= vectors; i++) {
            float[] feature = new float[FINGERPRINT];
            for (int j=0; j < FINGERPRINT; j++) {
                feature[j] = filteredSpectrum.get(i * shift + j);
            }
            tuples.add(this.phandler.generateTuple(segment.getId(), feature));
        }
        this.phandler.persist(tuples);
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
            if (feature[i] > 0) {
                weight[i] = 1.0f;
            }
        }
        return qc.clone().setDistanceWeights(weight);
    }

    /**
     * Merges the provided QueryConfig with the default QueryConfig enforced by the
     * feature module.
     *
     * @param qc QueryConfig provided by the caller of the feature module.
     * @param weights Weights to be used durign query.
     * @return Modified QueryConfig.
     */
    protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc, float[] weights) {
        return new QueryConfig(qc)
                .setCorrespondenceFunctionIfEmpty(this.linearCorrespondence)
                .setDistanceIfEmpty(QueryConfig.Distance.manhattan)
                .setDistanceWeights(weights);
    }

    /**
     *
     * @param segment
     * @return
     */
    private TIntArrayList filterSpectrum(SegmentContainer segment) {
        /* Prepare empty list of candidates for filtered spectrum. */
        TIntArrayList candidates = new TIntArrayList();

        /* Perform STFT and extract the Spectra. If this fails, return empty list. */
        Pair<Integer,Integer> properties = FFTUtil.parametersForDuration(segment.getSamplingrate(), WINDOW_SIZE);
        STFT stft = segment.getSTFT(properties.first, (properties.first-properties.second)/4, properties.second, new HanningWindow());
        if (stft == null) return candidates;
        List<Spectrum> spectra = stft.getPowerSpectrum();

        /* Foreach spectrum; find peak-values in the defined ranges. */
        for (Spectrum spectrum : spectra) {
            int spectrumidx = 0;
            for (int j = 0; j < RANGES.length - 1; j++) {
                Pair<Float, Double> peak = null;
                for (int k = spectrumidx; k < spectrum.size(); k++) {
                    Pair<Float, Double> bin = spectrum.get(k);
                    if (bin.first >= RANGES[j] && bin.first <= RANGES[j + 1]) {
                        if (peak == null || bin.second > peak.second) {
                            peak = bin;
                        }
                    } else if (bin.first > RANGES[j + 1]) {
                        spectrumidx = k;
                        break;
                    }
                }
                candidates.add(Math.round(peak.first - (peak.first.intValue() % 2)));
            }
        }
        return candidates;
    }
}
