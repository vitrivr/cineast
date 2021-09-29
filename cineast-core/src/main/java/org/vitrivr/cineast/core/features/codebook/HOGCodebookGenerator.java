package org.vitrivr.cineast.core.features.codebook;

import boofcv.abst.feature.dense.DescribeImageDense;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.GrayU8;
import org.ddogleg.clustering.FactoryClustering;
import org.vitrivr.cineast.core.util.images.HOGHelper;

import java.awt.image.BufferedImage;


public class HOGCodebookGenerator extends ImageCodebookGenerator {
    /**
     * Default constructor.
     */
    public HOGCodebookGenerator() {
        super(HOGHelper.hogVectorSize(HOGHelper.DEFAULT_CONFIG), true);
    }

    /**
     * Initializes the codebook generator (i.e. setup the clusterer etc.)
     */
    @Override
    protected void init() {
        this.clusterer = FactoryClustering.kMeans_F64(null, 200, 20, 1e-7);
    }

    /**
     * Processes the content (i.e. creates descriptors) and add the generated
     * descriptors to the cluster.
     *
     * @param content The image to process.
     */
    @Override
    protected void process(BufferedImage content) {
        DescribeImageDense<GrayU8, TupleDesc_F64> hog = HOGHelper.getHOGDescriptors(content);
        for (TupleDesc_F64 desc : hog.getDescriptions()) {
            this.cluster.addReference(desc);
        }
    }
}
