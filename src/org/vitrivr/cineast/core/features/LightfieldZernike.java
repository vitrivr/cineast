package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.images.ZernikeHelper;
import org.vitrivr.cineast.core.util.math.MathConstants;
import org.vitrivr.cineast.core.util.math.ZernikeMoments;

/**
 * An Extraction and Retrieval module for 3D models that leverages Zernike moment based light field descriptors and as proposed in [1] and [2].
 *
 * [1] Chen, D.-Y., Tian, X.-P., Shen, Y.-T., & Ouh. (2003).
 *      On Visual Similarity Based 3D Model Retrieval. In Eurographics (Vol. 22, pp. 313–318). http://doi.org/KE.2008.4730947
 *
 * [2] Zhang, D., & Lu, G. (2002).
 *      An Integrated Approach to Shape Based Image Retrieval. In ACCV2002: The 5th Asian Conference on Computer Vision. Melbourne, Australia.
 *
 * @author rgasser
 * @version 1.0
 * @created 16.02.17
 */
public class LightfieldZernike extends Lightfield {
    /** Size of the feature vector. */
    private static final int SIZE = 36 + 1; /* Number of Coefficients + Pose Idx */

    /**
     * Default constructor for LightfieldZernike class.
     */
    public LightfieldZernike() {
        super("features_lightfieldzernike", 2.0f, SIZE, MathConstants.VERTICES_3D_DODECAHEDRON);
    }

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

    /**
     * Extracts the Lightfield Fourier descriptors from a provided BufferedImage. The returned list contains
     * elements for each identified contour of adequate size.
     *
     * @param image Image for which to extract the Lightfield Fourier descriptors.
     * @param poseidx Poseidx of the extracted image.
     * @return List of descriptors for image.
     */
    @Override
    protected List<float[]> featureVectorsFromImage(BufferedImage image, int poseidx) {
        final List<ZernikeMoments> moments = ZernikeHelper.zernikeMomentsForShapes(image, RENDERING_SIZE /2, 10);
        final List<float[]> features = new ArrayList<>(moments.size());
        for (ZernikeMoments moment : moments) {
            float[] feature = new float[SIZE];
            int i = 0;
            for (Complex m : moment.getMoments()) {
                feature[i] = (float)m.abs();
                i++;
            }
            feature = MathHelper.normalizeL2InPlace(feature);
            feature[0] = poseidx;
            features.add(feature);
        }
        return features;
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
        List<ReadableQueryConfig> configs = new ArrayList<>(features.size());
        for (float[] feature : features) {
            if (feature[0] == POSEIDX_UNKNOWN) {
                configs.add(new QueryConfig(qc).setDistanceWeights(WEIGHTS_NOPOSE));
            } else {
                configs.add(new QueryConfig(qc).setDistanceWeights(WEIGHTS_POSE));
            }
        }
        return configs;
    }
}
