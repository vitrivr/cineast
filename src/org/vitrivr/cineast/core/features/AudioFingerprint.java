package org.vitrivr.cineast.core.features;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.*;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.*;

import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.fft.STFT;
import org.vitrivr.cineast.core.util.fft.Spectrum;
import org.vitrivr.cineast.core.util.fft.windows.HanningWindow;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.02.17
 */
public class AudioFingerprint implements Extractor, Retriever {

    /** Field names in the data-store. */
    private static final String[] FIELDS = {"id", "fingerprint"};

    /** NAme of the entity in the data-store. */
    private final String tableName = "features_audiofingerprint";

    /** Frequency-ranges that should be used to calculate the fingerprint. */
    private static final float[] RANGES = {20.0f, 40.0f, 80.0f, 120.0f, 180.0f, 300.0f, 420.0f};

    /** Length of an individual fingerprint (size of FV). */
    private static final int FINGERPRINT = 20 * (RANGES.length-1);


    protected DBSelector selector;
    protected PersistencyWriter<?> phandler;


    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        this.phandler = phandlerSupply.get();
        this.phandler.open(this.tableName);
        this.phandler.setFieldNames(FIELDS[0],FIELDS[1]);
    }

    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        this.selector = selectorSupply.get();
        this.selector.open(this.tableName);
    }

    @Override
    public void finish() {
        if(this.phandler != null){
            this.phandler.close();
            this.phandler = null;
        }

        if(this.selector != null){
            this.selector.close();
            this.selector = null;
        }
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createFeatureEntity(this.tableName, false, new AttributeDefinition(FIELDS[1], AttributeDefinition.AttributeType.VECTOR));
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(this.tableName);
    }




    @Override
    public List<StringDoublePair> getSimilar(String shotId, QueryConfig qc) {
        return null;
    }


    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        HashMap<String,Double> map = new HashMap<>();
        List<StringDoublePair> results = new ArrayList<>();


        qc.setDistance(QueryConfig.Distance.hamming);

        List<Pair<Float, Double>> filteredSpectrum = this.filterSpectrum(sc);
        int lookups = filteredSpectrum.size() / FINGERPRINT;

        SummaryStatistics statistics = new SummaryStatistics();

        float[] feature = new float[FINGERPRINT];
        for (int i=0;i<=lookups;i++) {
            float correction = 0.0f;
            for (int j=0; j< FINGERPRINT; j++) {
                if (i * lookups + j < filteredSpectrum.size()) {
                    float frequency = filteredSpectrum.get(i * lookups + j).first;
                    feature[j] = Math.round(frequency - ((int)frequency % 2));
                } else {
                    feature[j] = -1.0f;
                    correction += 1.0f;
                }
            }
            List<StringDoublePair> partials = this.selector.getNearestNeighbours(100, feature, "fingerprint", qc);
            for (StringDoublePair result : partials) {
                statistics.addValue(result.value);
                if (map.containsKey(result.key)) {
                    map.put(result.key, Math.min(result.value-correction, map.get(result.key)));
                } else {
                    map.put(result.key, result.value-correction);
                }
            }
        }

        map.forEach((key, value) -> {
            if (value < statistics.getMean()) {
                results.add(new StringDoublePair(key, MathHelper.getScore(value, statistics.getMax())));
            }
        });

        return results;
    }

    @Override
    public void processShot(SegmentContainer segment) {
        List<Pair<Float, Double>> filteredSpectrum = this.filterSpectrum(segment);

        int shift = RANGES.length-1;
        int vectors = (filteredSpectrum.size() - FINGERPRINT) / shift;
        List<PersistentTuple> tuples = new ArrayList<>();
        for (int i = 0; i <= vectors; i++) {
            float[] feature = new float[FINGERPRINT];
            for (int j=0; j < FINGERPRINT; j++) {
                float frequency = filteredSpectrum.get(i * shift + j).first;
                feature[j] = Math.round(frequency - ((int)frequency % 2));
            }
            tuples.add(this.phandler.generateTuple(segment.getId(), feature));
        }
        this.phandler.persist(tuples);
    }

    /**
     *
     * @param segment
     * @return
     */
    private List<Pair<Float, Double>> filterSpectrum(SegmentContainer segment) {
        /* Prepare empty list of candidates for filtered spectrum. */
        List<Pair<Float, Double>> candidates = new ArrayList<>();

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
                candidates.add(peak);
            }
        }
        return candidates;
    }
}
