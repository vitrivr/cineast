package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractCodebookFeatureModule;
import org.vitrivr.cineast.core.util.images.HOGHelper;

import boofcv.abst.feature.dense.DescribeImageDense;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.GrayU8;

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
        LOGGER.traceEntry();

        BufferedImage image = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
        if (image != null) {
            DescribeImageDense<GrayU8, TupleDesc_F64> hog = HOGHelper.getHOGDescriptors(image);
            if (hog != null && hog.getDescriptions().size() > 0) {
                float[] histogram_f = this.histogram(true, hog.getDescriptions());
                this.persist(shot.getId(), new FloatVectorImpl(histogram_f));
            } else {
                LOGGER.warn("No HOG feature could be extracted for segment {}. This is not necessarily an error!", shot.getId());
            }
        }

        LOGGER.debug("HOG.processShot() (codebook: {}) done in {}ms", this.codebook(), System.currentTimeMillis() - start);
        LOGGER.traceExit();
    }

    /**
     *
     * @param sc
     * @param qc
     * @return
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, ReadableQueryConfig rqc) {
        long start = System.currentTimeMillis();
        LOGGER.traceEntry();

        ReadableQueryConfig qc = setQueryConfig(rqc);

        List<StringDoublePair> results = null;
        BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();
        if (image != null) {
            DescribeImageDense<GrayU8, TupleDesc_F64> hog = HOGHelper.getHOGDescriptors(image);
            if (hog != null && hog.getDescriptions().size() > 0) {
                float[] histogram_f = this.histogram(true, hog.getDescriptions());
                results = this.getSimilar(histogram_f, qc);
            }
        }
        
        if (results == null){
          results = new ArrayList<>(0);
        }

        LOGGER.debug("HOG.getSimilar() (codebook: {}) done in {}ms", this.codebook(), (System.currentTimeMillis() - start));
        return LOGGER.traceExit(results);
    }
}
