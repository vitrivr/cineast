package org.vitrivr.cineast.core.features;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.image.GrayF32;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.AbstractCodebookFeatureModule;
import org.vitrivr.cineast.core.util.images.SURFHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.01.17
 */
public abstract class SURF extends AbstractCodebookFeatureModule {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * @param tableName
     * @param maxDist
     */
    protected SURF(String tableName, float maxDist) {
        super(tableName, maxDist);
    }

    @Override
    public void processShot(SegmentContainer shot) {
        long start = System.currentTimeMillis();
        LOGGER.entry();

        DetectDescribePoint<GrayF32, BrightFeature> descriptors = SURFHelper.getStableSurf(shot.getMostRepresentativeFrame().getImage().getBufferedImage());
        if (descriptors != null) {
            float[] histogram_f = this.histogram(true, descriptors);
            this.persist(shot.getId(), new FloatVectorImpl(histogram_f));
        } else {
            LOGGER.warn("Segment {} did not have a most representative frame. No descriptor has been generated!");
        }

        LOGGER.debug("SURF.processShot() (codebook {}) done in {}ms", this.codebook(), (System.currentTimeMillis() - start));
        LOGGER.exit();
    }

    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        long start = System.currentTimeMillis();
        LOGGER.entry();

        qc.setDistanceIfEmpty(QueryConfig.Distance.chisquared);

        List<StringDoublePair> results = null;
        DetectDescribePoint<GrayF32, BrightFeature> descriptors = SURFHelper.getStableSurf(sc.getAvgImg().getBufferedImage());
        if (descriptors != null) {
            float[] histogram_f = this.histogram(true, descriptors);
            results = this.getSimilar(histogram_f, qc);
        } else {
            results = new ArrayList<>();
        }

        LOGGER.debug("SURF.getSimilar() (codebook {}) done in {}ms", this.codebook(), (System.currentTimeMillis() - start));
        return LOGGER.exit(results);
    }
}
