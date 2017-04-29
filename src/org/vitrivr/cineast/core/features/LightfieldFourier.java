package org.vitrivr.cineast.core.features;

import boofcv.alg.filter.binary.Contour;
import georegression.struct.point.Point2D_I32;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
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
    /** Size of the feature vector. */
    private static final int SIZE = 128 + 1; /* Number of Coefficients + Pose Idx */

    /**
     * Weights used for kNN retrieval based on images / sketches. Higher frequency components (standing for finer details)
     * have less weight towards the final result.
     *
     * Also, the first entry (pose-idx) does count less towards the final distance, if known, and not at all if is unknown.
     */
    private static final float[] WEIGHTS_POSE = new float[SIZE];
    private static final float[] WEIGHTS_NOPOSE = new float[SIZE];
    static {
        WEIGHTS_POSE[0] = 1.50f;
        WEIGHTS_NOPOSE[0] = 0.0f;
        for (int i = 1; i< SIZE; i++) {
            WEIGHTS_POSE[i] = 1.0f - (i-2)*(1.0f/(2*SIZE));
            WEIGHTS_NOPOSE[i] = 1.0f - (i-2)*(1.0f/(2*SIZE));
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
     * Returns the modified QueryConfig for the provided feature vector. Creates a weighted
     * version of the original configuration.
     *
     * @param qc Original query config
     * @param feature Feature for which a weight-vector is required.
     * @return
     */
    protected ReadableQueryConfig queryConfigForFeature(QueryConfig qc, float[] feature) {
        if (feature[0] == POSEIDX_UNKNOWN) {
            return qc.clone().setDistanceWeights(WEIGHTS_NOPOSE);
        } else {
            return qc.clone().setDistanceWeights(WEIGHTS_POSE);
        }
    }


    /**
     * Extracts the Lightfield Fourier descriptors from a provided BufferedImage. The returned list contains
     * elements for each identified contour of adequate size.
     *
     * @param image Image for which to extract the Lightfield Fourier descriptors.
     * @param poseidx Poseidx of the extracted image.
     * @return List of descriptors for image.
     */
    protected List<float[]> featureVectorsFromImage(BufferedImage image, int poseidx) {
        final List<Contour> contours = ContourHelper.getContours(image);
        final List<float[]> features = new ArrayList<>();

        /* Select the largest, inner contour from the list of available contours. */
        for (Contour contour : contours) {
            for (List<Point2D_I32> inner : contour.internal) {
                /* Check size of selected contour. */
                if (inner.size() < SIZE * 2) continue;

                /* Calculate the descriptor for the selected contour. */
                double[] cds = ContourHelper.centroidDistance(inner, true);
                Complex[] results = this.transformer.transform(cds, TransformType.FORWARD);
                double magnitude = results[0].abs();
                float[] feature = new float[SIZE];
                for (int i = 1; i < SIZE; i++) {
                    feature[i] = (float) (results[i+1].abs() / magnitude);
                }
                feature = MathHelper.normalizeL2InPlace(feature);
                feature[0] = poseidx;
                features.add(feature);
            }
        }

        return features;
    }
}
