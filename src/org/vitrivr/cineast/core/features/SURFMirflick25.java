package org.vitrivr.cineast.core.features;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.image.GrayF32;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.*;
import org.vitrivr.cineast.core.features.abstracts.AbstractCodebookFeatureModule;
import org.vitrivr.cineast.core.util.features.SURFHelper;

import java.util.*;

/**
 * A Extraction and Retrieval modules that uses SURF descriptors and a Codebook based on MIRFLICK25 to obtain a
 * histograms of codewords. These histograms ares used as feature-vectors.
 *
 * @author rgasser
 * @version 1.0
 * @created 18.01.17
 */
public class SURFMirflick25 extends AbstractCodebookFeatureModule {
    /** */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Default constructor.
     */
    public SURFMirflick25() {
        super("features_surf_mirflick25", 2.0f);
    }

    @Override
    public void processShot(SegmentContainer shot) {
        long start = System.currentTimeMillis();
        LOGGER.entry();

        DetectDescribePoint<GrayF32, BrightFeature> descriptors = SURFHelper.getStableSurf(shot.getAvgImg().getBufferedImage());
        if (descriptors != null) {
            float[] histogram_f = this.histogram(true, descriptors);
            this.persist(shot.getId(), new FloatVectorImpl(histogram_f));
        }

        LOGGER.debug("SURF.processShot() done in {}ms", (System.currentTimeMillis() - start));
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

        LOGGER.debug("SURF.getSimilar() done in {}ms", (System.currentTimeMillis() - start));
        return LOGGER.exit(results);
    }


    /**
     * Returns the full name of the Codebook to use.
     *
     * @return
     */
    @Override
    protected String codebook() {
        return "mirflickr25.surfcb";
    }
}
