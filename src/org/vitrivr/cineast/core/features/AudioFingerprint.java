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
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.MathHelper;
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
public class AudioFingerprint extends AbstractFeatureModule {
    /** Frequency-ranges that should be used to calculate the fingerprint. */
    private static final float[] RANGES = {20.0f, 40.0f, 80.0f, 120.0f, 180.0f, 300.0f, 420.0f};

    /** Length of an individual fingerprint (size of FV). */
    private static final int FINGERPRINT = 20 * (RANGES.length-1);


    /**
     * Default constructor;
     */
    public AudioFingerprint() {
        super("features_audiofingerprint", 100.0f);
    }


    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        /* List that holds final results. */
        List<ScoreElement> results = new ArrayList<>();

        /* Map that holds partial results. */
        TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();

        /*
         * Configure query: Use a weighted Manhattan Distance. Weights are used to
         * to inactivate entries in short segments.
         */
        float[] weights = new float[FINGERPRINT];

        TIntArrayList filteredSpectrum = this.filterSpectrum(sc);
        int lookups = filteredSpectrum.size() / FINGERPRINT;

        SummaryStatistics statistics = new SummaryStatistics();

        float[] feature = new float[FINGERPRINT];
        for (int i=0;i<=lookups;i++) {
            for (int j=0; j< FINGERPRINT; j++) {
                if (i * lookups + j < filteredSpectrum.size()) {
                    feature[j] = filteredSpectrum.get(i * lookups + j);
                    weights[j] = 1.0f;
                } else {
                    weights[j] = 0.0f;
                }
            }
            List<SegmentDistanceElement> partials = this.selector.getNearestNeighbours(Config.sharedConfig().getRetriever().getMaxResultsPerModule()/4, feature, "fingerprint", SegmentDistanceElement.class, qc);
            for (SegmentDistanceElement result : partials) {
                statistics.addValue(result.getDistance());
                map.adjustOrPutValue(result.getSegmentId(), Math.min(result.getDistance(), map.get(result.getSegmentId())), result.getDistance());
            }
        }

        /* Set QueryConfig and extract correspondence function. */
        qc = this.setQueryConfig(qc);
        final CorrespondenceFunction correspondence = CorrespondenceFunction.linear(statistics.getMean());

        /* Prepare final results. */
        map.forEachEntry((key, value) -> {
            results.add(new SegmentScoreElement(key, correspondence.applyAsDouble(value)));
            return true;
        });
        return ScoreElement.filterMaximumScores(results.stream());
    }

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
        STFT stft = segment.getSTFT(4096, 0, new HanningWindow());
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
