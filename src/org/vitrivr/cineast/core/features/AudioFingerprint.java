package org.vitrivr.cineast.core.features;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.fft.STFT;
import org.vitrivr.cineast.core.util.fft.Spectrum;
import org.vitrivr.cineast.core.util.fft.windows.HanningWindow;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.02.17
 */
public class AudioFingerprint implements Extractor, Retriever { //FIXME why is this not an AbstractFratureModule?

  /** Field names in the data-store. */
  private static final String[] FIELDS = {"id", "fingerprint"};

  /** NAme of the entity in the data-store. */
  private final String tableName = "features_audiofingerprint";

  /** Frequency-ranges that should be used to calculate the fingerprint. */
  private static final float[] RANGES = {20.0f, 40.0f, 80.0f, 120.0f, 180.0f, 300.0f, 420.0f};

  /** Length of an individual fingerprint (size of FV). */
  private static final int FINGERPRINT = 20 * (RANGES.length - 1);


  protected DBSelector selector;
  protected PersistencyWriter<?> phandler;


  @Override
  public void init(PersistencyWriterSupplier phandlerSupply) {
    this.phandler = phandlerSupply.get();
    this.phandler.open(this.tableName);
    this.phandler.setFieldNames(FIELDS[0], FIELDS[1]);
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(this.tableName);
  }

  @Override
  public void finish() {
    if (this.phandler != null) {
      this.phandler.close();
      this.phandler = null;
    }

    if (this.selector != null) {
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
  public List<ScoreElement> getSimilar(String shotId, ReadableQueryConfig qc) {
    return null; //FIXME this should so something..?
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig rqc) {
    QueryConfig qc = new QueryConfig(rqc);
    qc.setDistanceIfEmpty(QueryConfig.Distance.hamming);

    List<Pair<Float, Double>> filteredSpectrum = this.filterSpectrum(sc);
    int lookups = filteredSpectrum.size() / FINGERPRINT;

    SummaryStatistics statistics = new SummaryStatistics();
    TObjectDoubleMap<String> minimumDistances = new TObjectDoubleHashMap<>();

    float[] feature = new float[FINGERPRINT];
    for (int i = 0; i <= lookups; i++) {
      float correction = 0.0f;
      for (int j = 0; j < FINGERPRINT; j++) {
        if (i * lookups + j < filteredSpectrum.size()) {
          float frequency = filteredSpectrum.get(i * lookups + j).first;
          feature[j] = Math.round(frequency - ((int) frequency % 2));
        } else {
          feature[j] = -1.0f;
          correction += 1.0f;
        }
      }

      List<SegmentDistanceElement> partials = this.selector
          .getNearestNeighbours(100, feature, "fingerprint", SegmentDistanceElement.class, qc);
      for (SegmentDistanceElement element : partials) {
        String id = element.getSegmentId();
        double distance = element.getDistance();
        double correctedDistance = distance - correction;
        statistics.addValue(distance);

        if (!minimumDistances.containsKey(id) || correctedDistance < minimumDistances.get(id)) {
          minimumDistances.put(id, correctedDistance);
        }
      }
    }

    return transformDistancesToScores(minimumDistances, statistics);
  }

  private List<ScoreElement> transformDistancesToScores(TObjectDoubleMap<String> distances,
      SummaryStatistics statistics) {
    CorrespondenceFunction linearFunction = CorrespondenceFunction.linear(statistics.getMax());
    List<ScoreElement> scores = new ArrayList<>();
    distances.forEachEntry((id, distance) -> {
      if (distance < statistics.getMean()) {
        double score = linearFunction.applyAsDouble(distance);
        scores.add(new SegmentScoreElement(id, score));
      }
      return true;
    });
    return scores;
  }

  @Override
  public void processShot(SegmentContainer segment) {
    List<Pair<Float, Double>> filteredSpectrum = this.filterSpectrum(segment);

    int shift = RANGES.length - 1;
    int vectors = (filteredSpectrum.size() - FINGERPRINT) / shift;
    List<PersistentTuple> tuples = new ArrayList<>();
    for (int i = 0; i <= vectors; i++) {
      float[] feature = new float[FINGERPRINT];
      for (int j = 0; j < FINGERPRINT; j++) {
        float frequency = filteredSpectrum.get(i * shift + j).first;
        feature[j] = Math.round(frequency - ((int) frequency % 2));
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
    /* Perform STFT and extract the Spectra. */
    STFT stft = segment.getSTFT(4096, 0, new HanningWindow());
    List<Spectrum> spectra = stft.getPowerSpectrum();

    /* List of candidates for filtered spectrum. */
    List<Pair<Float, Double>> candidates = new ArrayList<>();

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
