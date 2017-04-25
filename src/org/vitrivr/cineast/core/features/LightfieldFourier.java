package org.vitrivr.cineast.core.features;

import boofcv.alg.filter.binary.Contour;
import georegression.struct.point.Point2D_I32;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.images.ContourHelper;
import org.vitrivr.cineast.core.util.math.MathConstants;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.03.17
 */
public class LightfieldFourier extends Lightfield {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Weights used for kNN retrieval based on images / sketches. Higher frequency components (standing for finer details) will have less
     * weight towards the final result.
     */
    private static final float[] WEIGHTS = new float[SIZE+1];
    static {
        for (int i=0;i<SIZE;i++) {
            WEIGHTS[i] = 1.0f - (i-1)*(1.0f/(2*SIZE));
        }
    }

    /** Helper class that is used to perform FFT. */
    private final FastFourierTransformer transformer;

    /**
     * Default constructor for LightfieldFourier class.
     */
    public LightfieldFourier() {
        super("features_lightfieldfourier", 2.0f, MathConstants.VERTICES_3D_DODECAHEDRON);
        this.transformer = new FastFourierTransformer(DftNormalization.STANDARD);
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
                .setDistanceIfEmpty(QueryConfig.Distance.euclidean)
                .setDistanceWeights(WEIGHTS);
    }

    /**
     * Extracts the Lightfield Fourier descriptors from a provided BufferedImage. The returned list contains
     * elements for each identified contour of adequate size.
     *
     * @param image Image for which to extract the Lightfield Fourier descriptors.
     * @param poseidx Poseidx of the extracted image.
     * @return List of descriptors for image.
     */
    protected List<Pair<Integer,float[]>> featureVectorsFromImage(BufferedImage image, int poseidx) {
        List<Contour> contours = ContourHelper.getContours(image);
        List<Pair<Integer,float[]>> features = new ArrayList<>();

        int fv_size = 128;

        /* Select the largest, inner contour from the list of available contours. */
        for (Contour contour : contours) {
            for (List<Point2D_I32> inner : contour.internal) {
                /* Check size of selected contour. */
                if (inner.size() < fv_size * 2) continue;

                /* Calculate the descriptor for the selected contour. */
                double[] cds = ContourHelper.centroidDistance(inner, true);
                Complex[] results = this.transformer.transform(cds, TransformType.FORWARD);
                double magnitude = results[0].abs();
                float[] feature = new float[fv_size];
                for (int i = 0; i < fv_size; i++) {
                    feature[i] = (float) (results[i+1].abs() / magnitude);
                }
                features.add(new Pair<>(poseidx, MathHelper.normalizeL2(feature)));
            }
        }

        return features;
    }
}
