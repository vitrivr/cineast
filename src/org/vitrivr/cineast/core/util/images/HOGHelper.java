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
 * @author rgasser
 * @version 1.0
 * @created 22.01.17
 */
public class HOGHelper {

    /** Size of a single HOG descriptor. */
    public final static int HOG_VECTOR_SIZE = 128;

    /**
     *
     */
    private HOGHelper() {

    }

    /**
     * Returns HOG descriptors for an image using the provided settings. Uses the BoofCV fast SURF algorithm,
     * which yields less images but operates a bit faster.
     *
     * @param image Image for which to obtain the SURF descriptors.
     * @return
     */
    public static DescribeImageDense<GrayU8,TupleDesc_F64> getHOGDescriptors(BufferedImage image) {
        GrayU8 gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
        ConfigDenseHoG config= new ConfigDenseHoG();
        config.cellsPerBlockX = 4;
        config.cellsPerBlockY = 4;
        config.orientationBins = 8;
        config.fastVariant = false;
        DescribeImageDense<GrayU8,TupleDesc_F64> desc = FactoryDescribeImageDense.hog(config, ImageType.single(GrayU8.class));
        desc.process(gray);
        return desc;
    }
}
