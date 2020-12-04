package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;

import gnu.trove.list.array.TIntArrayList;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.features.abstracts.StagedFeatureModule;
import org.vitrivr.cineast.core.util.dsp.fft.FFTUtil;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.02.17
 */
public class AudioFingerprint extends StagedFeatureModule {
    /** Frequency-ranges that should be used to calculate the fingerprint. */
    private static final float[] RANGES = {30.0f, 40.0f, 80.0f, 120.0f, 180.0f, 300.0f, 480.0f};

    /** Length of an individual fingerprint (size of FV). */
    private static final int FINGERPRINT = 20 * (RANGES.length-1);

    /** Size of the window during STFT in seconds (as proposed in [2]). */
    private static final float WINDOW_SIZE = 0.2f;

    /**
     * Default constructor;
     */
    public AudioFingerprint() {
        super("features_audiofingerprint", 4000.0f, FINGERPRINT);
    }

    /**
     *
     * @param segment
     */
    @Override
    public void processSegment(SegmentContainer segment) {
        TIntArrayList filteredSpectrum = this.filterSpectrum(segment);
        int vectors = filteredSpectrum.size()/FINGERPRINT;
        List<PersistentTuple> tuples = new ArrayList<>();
        for (int i = 0; i < vectors; i++) {
            float[] feature = new float[FINGERPRINT];
            for (int j=0; j < FINGERPRINT; j++) {
                feature[j] = filteredSpectrum.get(i * FINGERPRINT + j);
            }
            tuples.add(this.phandler.generateTuple(segment.getId(), feature));
        }
        this.phandler.persist(tuples);
    }

    /**
     * This method represents the first step that's executed when processing query. The associated SegmentContainer is
     * examined and feature-vectors are being generated. The generated vectors are returned by this method together with an
     * optional weight-vector.
     *
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
        final TIntArrayList filteredSpectrum = this.filterSpectrum(sc);
        final int shift = RANGES.length-1;
        final int lookups = Math.max(1,Math.min(30, filteredSpectrum.size() - FINGERPRINT));

        for (int i=1;i<=lookups;i++) {
            float[] feature = new float[FINGERPRINT];
            for (int j=0; j< FINGERPRINT; j++) {
                if (i * shift + j < filteredSpectrum.size()) {
                    feature[j] = filteredSpectrum.get(i * shift + j);
                }
            }
            features.add(feature);
        }
        return features;
    }

    /**
     *
     * @param features A list of feature-vectors (usually generated in the first stage). For each feature, a lookup is executed. May be empty!
     * @param configs A ReadableQueryConfig object that contains query-related configuration parameters.
     * @return
     */
    @Override
    protected List<SegmentDistanceElement> lookup(List<float[]> features, List<ReadableQueryConfig> configs) {
        List<SegmentDistanceElement> partialResults;
        if (features.size() == 1) {
            partialResults = this.selector.getNearestNeighboursGeneric(configs.get(0).getResultsPerModule(), features.get(0), FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, configs.get(0));
        } else {
            partialResults = this.selector.getBatchedNearestNeighbours(configs.get(0).getResultsPerModule(), features, FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, configs);
        }
        return partialResults;
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
        /* Prepare empty list of results. */
        final ArrayList<ScoreElement> results = new ArrayList<>();
        final HashMap<String, DistanceElement> map = new HashMap<>();

        /* Merge into map for final results; select the minimum distance. */
        for (DistanceElement result : partialResults) {
            map.merge(result.getId(), result, (d1, d2)-> {
                if (d1.getDistance() > d2.getDistance()) {
                    return d2;
                } else {
                    return d1;
                }
            });
        }

        /* Return immediately if no partial results are available.  */
        if (map.isEmpty()) {
          return results;
        }

        /* Prepare final results. */
        final CorrespondenceFunction fkt = qc.getCorrespondenceFunction().orElse(this.correspondence);
        map.forEach((key, value) -> results.add(value.toScore(fkt)));
        return ScoreElement.filterMaximumScores(results.stream());
    }

    /**
     * Returns a list of QueryConfigs for the given list of features. By default, this method simply returns a list of the
     * same the provided config. However, this method can be re-implemented to e.g. add a static or dynamic weight vectors.
     *
     * @param qc Original query config
     * @param features List of features for which a QueryConfig is required.
     * @return New query config (may be identical to the original one).
     */
    @Override
    protected List<ReadableQueryConfig> generateQueryConfigsForFeatures(ReadableQueryConfig qc, List<float[]> features) {
        ArrayList<ReadableQueryConfig> configs = new ArrayList<>(features.size());
        for (float[] feature : features) {
            float[] weight = new float[feature.length];
            for (int i=0;i<feature.length;i++) {
                if (feature[i] > 0) {
                    weight[i] = 1.0f;
                } else if (feature[i] == 0) {
                    weight[i] = 0.1f;
                }
            }
            configs.add(new QueryConfig(qc).setDistanceWeights(weight));
        }
        return configs;
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
        STFT stft = segment.getSTFT(properties.first, (properties.first-2*properties.second)/2, properties.second, new HanningWindow());
        if (stft == null) {
          return candidates;
        }
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
                .setDistanceIfEmpty(QueryConfig.Distance.manhattan)
                .addHint(ReadableQueryConfig.Hints.inexact);
    }
}
