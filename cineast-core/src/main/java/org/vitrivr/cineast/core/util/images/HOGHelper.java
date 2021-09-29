package org.vitrivr.cineast.core.util.images;

import boofcv.abst.feature.dense.DescribeImageDense;
import boofcv.factory.feature.dense.ConfigDenseHoG;
import boofcv.factory.feature.dense.FactoryDescribeImageDense;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;

import java.awt.image.BufferedImage;

/**
 * This class provides methods to extract Histogram of Oriented Gradients (HOG) features from images using
 * the BoofCV library. The default settings for the HOG features are taken from [1].
 *
 * [1] Velmurugan, K., & Santosh Baboo, S. (2011).
 *  Image Retrieval using Harris Corners and Histogram of Oriented Gradients. International Journal of Computer Applications, 24(7), 6–10. Retrieved from http://www.ijcaonline.org/journal/number14/pxc387478.pdf
 *
 */
public final class HOGHelper {

    /** Default configuration for HOG descriptors according to [1]
     *
     * - Number of cells: 4 in each dimension
     * - Number of bins: 8
     * - Number of pixels per cell: 16 x 16
     */
    public static final ConfigDenseHoG DEFAULT_CONFIG = new ConfigDenseHoG();
    static {
        DEFAULT_CONFIG.pixelsPerCell = 16;
        DEFAULT_CONFIG.cellsPerBlockX = 4;
        DEFAULT_CONFIG.cellsPerBlockY = 4;
        DEFAULT_CONFIG.orientationBins = 8;
        DEFAULT_CONFIG.fastVariant = false;
    }

    /**
     * Private constructor; do not instantiate!
     */
    private HOGHelper() {

    }


    /**
     * Returns HOG descriptors for an image using the following default configuration.
     *
     * @param image Image for which to obtain the HOG descriptors.
     * @return DescribeImageDense object containing the HOG descriptor.
     */
    public static DescribeImageDense<GrayU8,TupleDesc_F64> getHOGDescriptors(BufferedImage image) {
         return getHOGDescriptors(image, DEFAULT_CONFIG);
    }

    /**
     * Returns HOG descriptors for an image using the provided settings.
     *
     * @param image Image for which to obtain the HOG descriptors.
     * @param config ConfigDenseHog object that specifies the parameters for the HOG algorithm.
     * @return DescribeImageDense object containing the HOG descriptor.
     */
    public static DescribeImageDense<GrayU8,TupleDesc_F64> getHOGDescriptors(BufferedImage image, ConfigDenseHoG config) {
        GrayU8 gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
        DescribeImageDense<GrayU8,TupleDesc_F64> desc = FactoryDescribeImageDense.hog(config, ImageType.single(GrayU8.class));
        desc.process(gray);
        return desc;
    }

    /**
     * Returns the size of a HOG vector given a configuration.
     *
     * @param config ConfigDenseHoG that describes the HOG configuration.
     * @return Size of an individual HOG feature.
     */
    public static int hogVectorSize (ConfigDenseHoG config) {
        return config.cellsPerBlockX * config.cellsPerBlockX * config.orientationBins;
    }
}
