package org.vitrivr.cineast.core.features;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.image.GrayF32;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
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


    private static QueryConfig.Distance DEFAULT_DISTANCE = QueryConfig.Distance.chisquared;

    /**
     * @param tableName
     */
    protected SURF(String tableName) {
        super(tableName, 2.0f);
    }

    @Override
    public void processShot(SegmentContainer shot) {
        long start = System.currentTimeMillis();
        LOGGER.traceEntry();

        DetectDescribePoint<GrayF32, BrightFeature> descriptors = SURFHelper.getStableSurf(shot.getMostRepresentativeFrame().getImage().getBufferedImage());
        if (descriptors != null && descriptors.getNumberOfFeatures() > 0) {
            float[] histogram_f = this.histogram(true, descriptors);
            this.persist(shot.getId(), new FloatVectorImpl(histogram_f));
        } else {
            LOGGER.warn("No SURF feature could be extracted for segment {}. This is not necessarily an error!", shot.getId());
        }

        LOGGER.debug("SURF.processShot() (codebook {}) done in {}ms", this.codebook(), (System.currentTimeMillis() - start));
        LOGGER.traceExit();
    }

    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        long start = System.currentTimeMillis();
        LOGGER.traceEntry();

        qc.setDistance(DEFAULT_DISTANCE);

        List<StringDoublePair> results = null;
        DetectDescribePoint<GrayF32, BrightFeature> descriptors = SURFHelper.getStableSurf(sc.getAvgImg().getBufferedImage());
        if (descriptors != null && descriptors.getNumberOfFeatures() > 0) {
            float[] histogram_f = this.histogram(true, descriptors);
            results = this.getSimilar(histogram_f, qc);
        } else {
            results = new ArrayList<>();
        }

        LOGGER.debug("SURF.getSimilar() (codebook {}) done in {}ms", this.codebook(), (System.currentTimeMillis() - start));
        return LOGGER.traceExit(results);
    }
}
