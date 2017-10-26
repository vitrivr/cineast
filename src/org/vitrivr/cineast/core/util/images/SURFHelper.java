package org.vitrivr.cineast.core.util.images;

import java.awt.image.BufferedImage;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.image.GrayF32;

/**
 * Extracts images based on the SURF algorithm described in [1]. For feature detection, the Fast Hession (FH-9) algorithm is used.
 *
 * [1] Herbert Bay, Andreas Ess, Tinne Tuytelaars, and Luc Van Gool, "Speeded-Up Robust Features (SURF)", CVIU June, 2008, Volume 110, Issue 3, pages 346-359
 *
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public final class SURFHelper {

    /** Number of Octaves to consider in the interest point detection step (Fast Hessian) of SURF. Defaults to 4 for FH-9 described in [1]. */
    private final static int FH_NUMBER_OF_OCTAVES = 4;

    /** Number of scales to consider in the interest point detection step (Fast Hessian)  of SURF. Defaults to 4 for FH-9 described in [1]. */
    private final static int FH_NUMBER_SCALES_PER_OCTAVE = 4;

    /** How often pixels are sampled in the first octave during the interest point detection step (Fast Hessian) of SURF. Defaults to 1 for FH-9 described in [1] */
    private final static int FH_INITIAL_SAMPLE_SIZE = 1;

    /** Size/width of the smallest feature/kernel in the first octave during the interest point detection step (Fast Hessian)  f SURF. Defaults to 9 for FH-9 described in [1] */
    private final static int FH_INITIAL_SIZE = 9;

    /** Limits the number of images obtained per scale and thus the number images persisted during the extraction. */
    private final static int FH_MAX_FEATURES_PER_SCALE = -1;

    /** Size of the SURF descriptor. */
    public final static int SURF_VECTOR_SIZE = 64;

    /**
     * Private constructor; do not instantiate!
     */
    private SURFHelper() {

    }

    /**
     * Returns SURF descriptors for an image using the settings above. Uses the BoofCV stable SURF algorithm.
     *
     * @param image Image for which to obtain the SURF descriptors.
     * @return
     */
    public static DetectDescribePoint<GrayF32, BrightFeature> getStableSurf(BufferedImage image) {
         /* Obtain raw SURF descriptors using the configuration above (FH-9 according to [1]). */
        GrayF32 gray = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);
        ConfigFastHessian config = new ConfigFastHessian(0, 2, FH_MAX_FEATURES_PER_SCALE, FH_INITIAL_SAMPLE_SIZE, FH_INITIAL_SIZE, FH_NUMBER_SCALES_PER_OCTAVE, FH_NUMBER_OF_OCTAVES);
        DetectDescribePoint<GrayF32, BrightFeature> surf = FactoryDetectDescribe.surfStable(config, null, null, GrayF32.class);
        surf.detect(gray);
        return surf;
    }

    /**
     * Returns SURF descriptors for an image using the settings above. Uses the BoofCV fast SURF algorithm,
     * which yields less images but operates a bit faster.
     *
     * @param image Image for which to obtain the SURF descriptors.
     * @return
     */
    public static DetectDescribePoint<GrayF32, BrightFeature> getFastSurf(BufferedImage image) {
         /* Obtain raw SURF descriptors using the configuration above (FH-9 according to [1]). */
        GrayF32 gray = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);
        ConfigFastHessian config = new ConfigFastHessian(0, 2, FH_MAX_FEATURES_PER_SCALE, FH_INITIAL_SAMPLE_SIZE, FH_INITIAL_SIZE, FH_NUMBER_SCALES_PER_OCTAVE, FH_NUMBER_OF_OCTAVES);
        DetectDescribePoint<GrayF32, BrightFeature> surf = FactoryDetectDescribe.surfFast(config, null, null, GrayF32.class);
        surf.detect(gray);
        return surf;
    }
}
