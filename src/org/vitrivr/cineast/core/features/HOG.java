package org.vitrivr.cineast.core.features;

import boofcv.abst.feature.dense.DescribeImageDense;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.GrayU8;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.AbstractCodebookFeatureModule;
import org.vitrivr.cineast.core.util.images.HOGHelper;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.01.17
 */
public abstract class HOG extends AbstractCodebookFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * @param tableName
     * @param maxDist
     */
    protected HOG(String tableName, float maxDist) {
        super(tableName, maxDist);
    }

    /**
     *
     * @param shot
     */
    @Override
    public void processShot(SegmentContainer shot) {
        long start = System.currentTimeMillis();
        LOGGER.entry();

        BufferedImage image = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
        if (image != null) {
            DescribeImageDense<GrayU8, TupleDesc_F64> hog = HOGHelper.getHOGDescriptors(image);
            if (hog != null) {
                float[] histogram_f = this.histogram(true, hog.getDescriptions());
                this.persist(shot.getId(), new FloatVectorImpl(histogram_f));
            } else {
                LOGGER.warn("Segment {} did not have a most representative frame. No descriptor has been generated!");
            }
        }

        LOGGER.debug("HOG.processShot() (codebook: {}) done in {}ms", this.codebook(), System.currentTimeMillis() - start);
        LOGGER.exit();
    }

    /**
     *
     * @param sc
     * @param qc
     * @return
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        long start = System.currentTimeMillis();
        LOGGER.entry();

        qc.setDistanceIfEmpty(QueryConfig.Distance.euclidean);

        List<StringDoublePair> results =  new ArrayList<>(Config.sharedConfig().getRetriever().getMaxResultsPerModule());
        BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();
        if (image != null) {
            DescribeImageDense<GrayU8, TupleDesc_F64> hog = HOGHelper.getHOGDescriptors(image);
            if (hog != null) {
                float[] histogram_f = this.histogram(true, hog.getDescriptions());
                results.addAll(this.getSimilar(histogram_f, qc));
            }
        }

        LOGGER.debug("HOG.getSimilar() (codebook: {}) done in {}ms", this.codebook(), (System.currentTimeMillis() - start));
        return LOGGER.exit(results);
    }
}
