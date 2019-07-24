package org.vitrivr.cineast.core.features.codebook;

import java.awt.image.BufferedImage;

import org.ddogleg.clustering.FactoryClustering;
import org.vitrivr.cineast.core.util.images.SURFHelper;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.image.GrayF32;

/**
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public class SURFCodebookGenerator extends ImageCodebookGenerator {
    /**
     * Default constructor.
     */
    public SURFCodebookGenerator() {
        super(SURFHelper.SURF_VECTOR_SIZE, true);
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
        DetectDescribePoint<GrayF32, BrightFeature> surf = SURFHelper.getFastSurf(content);
        for (int i=0;i<surf.getNumberOfFeatures();i++) {
            this.cluster.addReference(surf.getDescription(i));
        }
    }
}
