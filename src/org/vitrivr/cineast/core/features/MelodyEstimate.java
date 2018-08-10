package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.StagedFeatureModule;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.audio.pitch.Melody;
import org.vitrivr.cineast.core.util.audio.pitch.Pitch;
import org.vitrivr.cineast.core.util.audio.pitch.estimation.KLF0PitchEstimator;
import org.vitrivr.cineast.core.util.audio.pitch.tracking.PitchTracker;
import org.vitrivr.cineast.core.util.dsp.fft.FFTUtil;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;
import org.vitrivr.cineast.core.util.dsp.filter.frequency.SpectralWhiteningFilter;
import org.vitrivr.cineast.core.util.dsp.midi.MidiUtil;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.04.17
 */
public class MelodyEstimate extends StagedFeatureModule {

    /**
     * Size of a shingle in number of HPCP features.
     *
     * The final feature vector has SHINGLE_SIZE * HPCP.Resolution.Bins components.
     */
    private final static int SHINGLE_SIZE = 10;

    /** Size of the window during STFT in seconds. */
    private final static float WINDOW_SIZE = 0.048f;

    /** F0 / Pitch estimator class. */
    private final KLF0PitchEstimator estimator = new KLF0PitchEstimator();

    /** PitchTracker instance used for pitch-tracking. */
    private final PitchTracker tracker = new PitchTracker();

    /**
     *
     */
    public MelodyEstimate() {
        super("feature_melodyestimate", 2.0f);
    }

    /**
     * Processes a SegmentContainer for later persisting it in the storage layer.
     *
     * @param sc The SegmentContainer that should be processed.
     */
    @Override
    public void processSegment(SegmentContainer sc) {
        Melody melody = this.transcribe(sc);
        if(melody == null){
            return;
        }
        List<float[]> features = this.getFeatures(melody);
        for (float[] feature : features) {
            this.persist(sc.getId(), new FloatVectorImpl(feature));
        }
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
        Melody melody = this.transcribe(sc);
        if(melody == null){
            LOGGER.debug("No melody, skipping");
            return null;
        }
        return this.getFeatures(melody);
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
        /* TODO: Improve... */
        final CorrespondenceFunction correspondence = qc.getCorrespondenceFunction().orElse(this.correspondence);
        return ScoreElement.filterMaximumScores(partialResults.stream().map(d -> d.toScore(correspondence)));
    }

    /**
     * Merges the provided QueryConfig with the default QueryConfig enforced by the
     * feature module.
     *
     * @param qc QueryConfig provided by the caller of the feature module.
     * @return Modified QueryConfig.
     */
    @Override
    protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
        return new QueryConfig(qc)
                .setCorrespondenceFunctionIfEmpty(this.correspondence)
                .setDistanceIfEmpty(QueryConfig.Distance.euclidean);
    }

    /**
     * Generates a set of feature vectors for the provided melody and returns them
     *
     * @param melody Melody for which to generate the vectors.
     * @return
     */
    private List<float[]> getFeatures(Melody melody) {

        /* TODO: Improve... */

        /* Prepare empty features array. */
        final int vectors = Math.max((melody.size()-2) - SHINGLE_SIZE, 0);
        final List<float[]> features = new ArrayList<>(vectors);

        /* Preapre feature vectors by averaging 3 adjacents vectors. */
        for (int n = 0; n < vectors; n++) {
            float[] hist = new float[SHINGLE_SIZE * 12];
            for (int i=1+n; i<=1+n+SHINGLE_SIZE; i++) {
                for (int j=i-1; j<=i+1; j++) {
                    Pitch pitch = melody.getPitch(j);
                    int idx = MidiUtil.frequencyToMidi(pitch.getFrequency()) % 12;
                    hist[(i-n-1) * SHINGLE_SIZE + idx] += pitch.getSalience();
                }
            }
            if (MathHelper.checkNotZero(hist) && MathHelper.checkNotNaN(hist)) {
                features.add(MathHelper.normalizeL2(hist));
            }
        }

        return features;
    }

    /**
     *
     * @param sc
     * @return
     */
    private Melody transcribe(SegmentContainer sc) {
        /* Calculate STFT and apply spectral whitening. */
        Pair<Integer, Integer> parameters = FFTUtil.parametersForDuration(sc.getSamplingrate(), WINDOW_SIZE);
        STFT stft = sc.getSTFT(parameters.first, 0, parameters.second, new HanningWindow());
        stft.applyFilter(new SpectralWhiteningFilter(stft.getWindowsize(), stft.getSamplingrate(), 0.33f, 30));

        float time = stft.timeStepsize();

        /* Prepare necessary helper data-structures. */
        final List<List<Pitch>> s = this.estimator.estimatePitch(stft);
        this.tracker.initialize(s, time);
        this.tracker.trackPitches();
        return this.tracker.extractMelody(10);
    }
}
