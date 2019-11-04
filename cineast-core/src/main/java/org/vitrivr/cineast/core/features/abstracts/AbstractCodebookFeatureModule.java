package org.vitrivr.cineast.core.features.abstracts;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.alg.scene.FeatureToWordHistogram_F64;
import boofcv.io.UtilIO;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.GrayF32;
import org.ddogleg.clustering.AssignCluster;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;

import java.util.List;

/**
 * An abstract feature module that leverages a named codebook and a set of features to obtain
 * a histogram of codewords. It remains to the implementer which codebook and what descriptors to use.
 * Once features have been obtained, use the histogram() method to get the histogram given the corpus.
 *
 * All codebooks should be placed in the ./resources/codebooks folder.
 *
 * This class currently requires BoofCV.
 *
 * TODO: Use CSV based format for codebooks.
 *
 * @author rgasser
 * @version 1.0
 * @created 20.01.17
 */
public abstract class AbstractCodebookFeatureModule extends StagedFeatureModule {
    /** The Assignment used for the codebook. */
    private AssignCluster<double[]> assignment;

    /** The folder that contains the Codebook(s). */
    private static String CODEBOOK_FOLDER = "resources/codebooks/";

    /**
     *
     * @param tableName
     * @param maxDist
     */
    protected AbstractCodebookFeatureModule(String tableName, float maxDist, int vectorLength) {
        super(tableName, maxDist, vectorLength);
    }

    /**
     * Initializer for Extraction - must load the codebook.
     *
     * @param phandlerSupply
     */
    @Override
    public final void init(PersistencyWriterSupplier phandlerSupply) {
        super.init(phandlerSupply);

        /* Load the Codebook. */
        this.assignment = UtilIO.load(CODEBOOK_FOLDER + this.codebook());
    }

    /**
     * Initializer for Retrieval - must load the codebook.
     *
     * @param selectorSupply
     */
    @Override
    public final void init(DBSelectorSupplier selectorSupply) {
        super.init(selectorSupply);

        /* Load the Codebook. */
        this.assignment = UtilIO.load(CODEBOOK_FOLDER + this.codebook());
    }

    /**
     * Returns a histogram given the provided descriptors and the assignment object loaded
     * from the codebook.
     *
     * @param hard Indicates whether to use hard or soft assignment.
     * @param descriptors Feature descriptors.
     * @return float[] array with codebook
     */
    protected final float[] histogram(boolean hard, DetectDescribePoint<GrayF32, BrightFeature> descriptors) {
        /* Create new  Histogram-Calculator. */
        FeatureToWordHistogram_F64 histogram = new FeatureToWordHistogram_F64(this.assignment, hard);

        /* Add the features to the Histogram-Calculator... */
        for (int i=0;i<descriptors.getNumberOfFeatures();i++) {
            histogram.addFeature(descriptors.getDescription(i));
        }

        /* ... and calculates and returns the histogram. */
        histogram.process();
        return this.floatToDoubleArray(histogram.getHistogram());
    }

    /**
     * Returns a histogram given the provided descriptors and the assignment object loaded
     * from the codebook.
     *
     * @param hard Indicates whether to use hard or soft assignment.
     * @param descriptors Feature descriptors as List of TupleDesc_F64
     * @return float[] array with codebook
     */
    protected final float[] histogram(boolean hard, List<TupleDesc_F64> descriptors) {
        /* Create new  Histogram-Calculator. */
        FeatureToWordHistogram_F64 histogram = new FeatureToWordHistogram_F64(this.assignment, hard);

        /* Add the features to the Histogram-Calculator... */
        for (TupleDesc_F64 descriptor : descriptors) {
            histogram.addFeature(descriptor);
        }

        /* ... and calculates and returns the histogram. */
        histogram.process();
        return this.floatToDoubleArray(histogram.getHistogram());
    }

    /**
     * Converts a double array into a float array of the same size.
     *
     * @param dbl double array to be converted..
     * @return float array
     */
    protected final float[] floatToDoubleArray(double[] dbl) {
        float[] flt = new float[dbl.length];
        for (int i=0;i<dbl.length;i++) {
            flt[i] = (float)dbl[i];
        }
        return flt;
    }

    /**
     * Returns the full name of the codebook to use. All codebook be placed in the
     * ./resources/codebooks folder.
     *
     * @return
     */
    protected abstract String codebook();

    @Override
    protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
      return new QueryConfig(qc).setDistanceIfEmpty(QueryConfig.Distance.euclidean);
    }
}
