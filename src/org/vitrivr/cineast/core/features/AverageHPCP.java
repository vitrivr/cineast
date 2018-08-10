package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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

/**
 * @author rgasser
 * @version 1.0
 * @created 25.04.17
 */
public abstract class AverageHPCP extends StagedFeatureModule {
    /** Size of the window during STFT in samples. */
    private final static float WINDOW_SIZE = 0.2f;

    /** Minimum resolution to consider in HPCP calculation. */
    private final float min_frequency;

    /** Maximum resolution to consider in HPCP calculation. */
    private final float max_frequency;

    /** HPCP resolution (12, 24, 36 bins). */
    private final HPCP.Resolution resolution;

    /* The number of HPCP frames that should be averaged into a single vector. */
    private final int average;

    /**
     * Default constructor.
     *
     * @param name Name of the entity (for persistence writer).
     * @param min_frequency Minimum frequency to consider for HPCP generation.
     * @param max_frequency Maximum frequency to consider for HPCP generation.
     * @param resolution Resolution of HPCP (i.e. number of HPCP bins).
     * @param average Number of frames to average.
     */
    protected AverageHPCP(String name, float min_frequency, float max_frequency, HPCP.Resolution resolution, int average) {
        super(name, 2.0f);

        /* Assign variables. */
        this.min_frequency = min_frequency;
        this.max_frequency = max_frequency;
        this.resolution = resolution;
        this.average = average;
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
     * @return List of feature vectors for lookup.
     */
    @Override
    protected List<float[]> preprocessQuery(SegmentContainer sc, ReadableQueryConfig qc) {
        return this.getFeatures(sc);
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
        /* Prepare helper data-structures. */
        final HashMap<String,DistanceElement> map = new HashMap<>();

        /* Set QueryConfig and extract correspondence function. */
        for (DistanceElement hit : partialResults) {
            map.merge(hit.getId(), hit, (o,n) -> new SegmentDistanceElement(hit.getId(), (o.getDistance() + n.getDistance())/2));
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
        Pair<Integer,Integer> parameters = FFTUtil.parametersForDuration(segment.getSamplingrate(), WINDOW_SIZE);
        STFT stft = segment.getSTFT(parameters.first, (parameters.first-2*parameters.second)/2,parameters.second, new HanningWindow());
        if (stft == null) {
          return new ArrayList<>();
        }

        HPCP hpcps = new HPCP(this.resolution, this.min_frequency, this.max_frequency);
        hpcps.addContribution(stft);

        /* Determine number of vectors that will result from the data. */
        int vectors = hpcps.size()/this.average;

        List<float[]> features = new ArrayList<>(vectors);
        for (int i = 0; i < vectors; i++) {
            float[] feature = new float[2*this.resolution.bins];
            SummaryStatistics[] statistics = new SummaryStatistics[this.resolution.bins];
            for (int j = 0; j<this.average; j++) {
                for (int k=0; k<this.resolution.bins;k++) {
                    if (statistics[k] == null) {
                      statistics[k] = new SummaryStatistics();
                    }
                    statistics[k].addValue(hpcps.getHpcp(i*this.average + j)[k]);
                }
            }
            for (int k=0; k<this.resolution.bins;k++) {
                feature[2*k] = (float)statistics[k].getMean();
                feature[2*k+1] = (float)statistics[k].getStandardDeviation();
            }
            features.add(MathHelper.normalizeL2(feature));
        }
        return features;
    }
}
