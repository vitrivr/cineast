package org.vitrivr.cineast.core.features;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.alg.bow.ClusterVisualWords;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.image.GrayF32;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.clustering.FactoryClustering;
import org.ddogleg.clustering.kmeans.AssignKMeans_F64;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.*;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Extracts features based on the SURF algorithm described in [1]. For feature detection, the
 * Fast Hession (FH-9) algorithm is used.
 *
 * [1] Herbert Bay, Andreas Ess, Tinne Tuytelaars, and Luc Van Gool, "Speeded-Up Robust Features (SURF)", CVIU June, 2008, Volume 110, Issue 3, pages 346-359
 *
 * @author rgasser
 * @version 1.0
 * @created 18.01.17
 */
public class SURF extends AbstractFeatureModule {

    /** Number of Octaves to consider in the interest point detection step (Fast Hessian) of SURF. Defaults to 4 for FH-9 described in [1]. */
    private final static int FH_NUMBER_OF_OCTAVES = 4;

    /** Number of scales to consider in the interest point detection step (Fast Hessian)  of SURF. Defaults to 4 for FH-9 described in [1]. */
    private final static int FH_NUMBER_SCALES_PER_OCTAVE = 4;

    /** How often pixels are sampled in the first octave during the interest point detection step (Fast Hessian) of SURF. Defaults to 1 for FH-9 described in [1] */
    private final static int FH_INITIAL_SAMPLE_SIZE = 1;

    /** Size/width of the smallest feature/kernel in the first octave during the interest point detection step (Fast Hessian)  f SURF. Defaults to 9 for FH-9 described in [1] */
    private final static int FH_INITIAL_SIZE = 9;

    /** Limits the number of features obtained per scale and thus the number features persisted during the extraction. Defaults to -1 (unlimited) for FH-9 described in [1]. */
    private final static int FH_MAX_FEATURES_PER_SCALE = -1;

    /** Size of the visual vocabulary at extraction time. Value is 250 which result in approximately a 512KB feature per image.
     *
     * TODO: Finetune!
     */
    private final static int EX_VOCABULARY_SIZE = 250;

    /** Size of the visual vocabulary at extraction time. Value is 250 which result in approximately a 512KB feature per image.
     *
     * TODO: Finetune!
     */
    private final static int Q_VOCABULARY_SIZE = 10;

    /** Size of the SURF descriptor. */
    private final static int SURF_VECTOR_SIZE = 64;

    /** Configuration for FH-9 POI detector. */
    private static final ConfigFastHessian FAST_HESSIAN = new ConfigFastHessian(0.1f, 5, FH_MAX_FEATURES_PER_SCALE, FH_INITIAL_SAMPLE_SIZE, FH_INITIAL_SIZE, FH_NUMBER_SCALES_PER_OCTAVE, FH_NUMBER_OF_OCTAVES);


    private static final Logger LOGGER = LogManager.getLogger();

    public SURF() {
        super("features_surf", 2.0f);
    }

    @Override
    public void processShot(SegmentContainer shot) {
        long start = System.currentTimeMillis();
        LOGGER.entry();

        BufferedImage image = shot.getAvgImg().getBufferedImage();
        if (image != null) {
            this.persist(shot.getId(), this.obtainCondensedSurfAsFloatVector(image, EX_VOCABULARY_SIZE));
        }

        LOGGER.debug("SURF.processShot() done in {}ms", (System.currentTimeMillis() - start));
        LOGGER.exit();
    }

    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        long start = System.currentTimeMillis();
        LOGGER.entry();

        qc.setDistanceIfEmpty(QueryConfig.Distance.euclidean);

        BufferedImage image = sc.getAvgImg().getBufferedImage();
        List<ReadableFloatVector> descriptors = this.obtainCondensedSurfAsFloatVector(image, Q_VOCABULARY_SIZE);
        HashMap<String, StringDoublePair> results = new HashMap<>(Config.getRetrieverConfig().getMaxResultsPerModule());
        float[] cache = new float[SURF_VECTOR_SIZE];
        for (ReadableFloatVector descriptor : descriptors) {
            List<StringDoublePair> partial = this.getSimilar(descriptor.toArray(cache), qc);
            for (StringDoublePair result : partial) {
                if (results.containsKey(result.key)) {
                    if (result.value > results.get(result.key).value) {
                        results.replace(result.key, result);
                    }
                } else {
                    results.put(result.key, result);
                }
            }
        }

        /* Aggregate into a sorted list and limit that list. */
        List<StringDoublePair> resultslist = results.values().stream()
                .sorted(StringDoublePair.COMPARATOR)
                .limit(Config.getRetrieverConfig().getMaxResultsPerModule()).collect(Collectors.toList());

        LOGGER.debug("SURF.getSimilar() done in {}ms", (System.currentTimeMillis() - start));
        return LOGGER.exit(resultslist);
    }

    /**
     * Obtains a set of fast-surf descriptors for a provided image. The descriptor is returned
     * as List of ReadableFloatVectors.
     *
     * @param image Image for which to obtain the descriptors.
     * @return List of ReadableFloatVector's, each representing a local descriptor.
     */
    private List<ReadableFloatVector> obtainCondensedSurfAsFloatVector(BufferedImage image, int vocabularysize) {
        /* Obtain raw SURF descriptors using the configuration above (FH-9 according to [1]). */
        GrayF32 gray = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);
        DetectDescribePoint<GrayF32, BrightFeature> surf = FactoryDetectDescribe.surfStable(FAST_HESSIAN, null, null, GrayF32.class);
        surf.detect(gray);

        List<ReadableFloatVector> results = new ArrayList<>(vocabularysize);

        /* Now cluster the SURF descriptors. */
        if (surf.getNumberOfFeatures() > 0) {
             /* Now cluster the SURF descriptors to reduce the number of feature-vectors. */
            ComputeClusters<double[]> clusterer = FactoryClustering.kMeans_F64(null, 100, 20, 1e-6);
            ClusterVisualWords cluster = new ClusterVisualWords(clusterer, SURF_VECTOR_SIZE,0xABCDEF12);
            for (int i = 0;i<surf.getNumberOfFeatures();i++) {
                cluster.addReference(surf.getDescription(i));
            }
            cluster.process(vocabularysize);

            /* Extract the clusters and return those as new, compact features. */
            List<double[]> clusters = ((AssignKMeans_F64)(cluster.getAssignment())).getClusters();
            float f[] = new float[SURF_VECTOR_SIZE];
            for (double[] qv : clusters) {
                for (int j = 0; j < SURF_VECTOR_SIZE; j++) {
                    f[j] = (float)qv[j];
                }
                results.add(new FloatVectorImpl(f));
            }
        }

        return results;
    }
}
