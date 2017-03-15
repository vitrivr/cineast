package org.vitrivr.cineast.core.features;

import boofcv.alg.filter.binary.Contour;
import com.twelvemonkeys.image.ImageUtil;
import georegression.struct.point.Point2D_I32;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.*;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.images.ContourHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

import static org.vitrivr.cineast.core.util.math.MathConstants.VERTICES_3D_DODECAHEDRON;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.03.17
 */
public class LightfieldFourier implements Retriever, Extractor {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Field names of the entity associated with this feature module. */
    private  final static String[] FIELDS = {"id", "feature", "poseidx"};

    /** Name of the table associated with this feature module. */
    private final static String TABLE_NAME = "features_lightfieldfourier";

    /** Maximum distance for features retrieved by the feature module. */
    private final static float MAX_DIST = 2.0f;

    /** Default value for an unknown pose index. */
    private final static int POSEIDX_UNKNOWN = -1;

    /** Size of the feature's vector.  */
    private final static int SIZE = 128;

    /** Weights used for kNN retrieval based on images / sketches. Higher frequency components (standing for finer details) will have less
     * weight towards the final result. */
    private static final float[] WEIGHTS = new float[SIZE+1];
    static {
        for (int i=0;i<SIZE;i++) {
            WEIGHTS[i] = 1.0f - (i-1)*(1.0f/(2*SIZE));
        }
    }

    /** Offscreen rendering environment used to create Lightfield images. */
    private final JOGLOffscreenRenderer renderer;

    /** Helper class that is used to perform FFT. */
    private final FastFourierTransformer transformer;

    /** Helper class that is used to read from persistence layer. */
    private DBSelector selector;

    /** Helper class that is used to write to persistence layer. */
    private PersistencyWriter<?> phandler;

    /**
     * Default constructor for LightfieldDescriptor.
     */
    public LightfieldFourier() {
        this.transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        this.renderer = new JOGLOffscreenRenderer(SIZE * 2, SIZE * 2);
    }

    /**
     *
     * @param sc
     * @param qc
     * @return
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {

        /* Initialize helper data structures. */
        TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), 0.5f, 0.0f);
        TObjectDoubleHashMap<String> partialMap = new TObjectDoubleHashMap<>(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), 0.5f, 0.0f);


        List<StringDoublePair> results = new ArrayList<>();

        qc.setDistance(QueryConfig.Distance.manhattan);


        List<Pair<Integer,float[]>> features;

        /* Extract features from either the provided Mesh (1) or image (2). */
        Mesh mesh = sc.getNormalizedMesh();
        if (mesh.isEmpty()) {
            BufferedImage image = ImageUtil.createResampled(sc.getAvgImg().getBufferedImage(), SIZE * 2, SIZE * 2, Image.SCALE_SMOOTH);
            features = this.featureVectorsFromImage(image,POSEIDX_UNKNOWN);
            qc.setDistanceWeights(WEIGHTS);
        } else {
            features = this.featureVectorsFromMesh(mesh);
        }

        /* */
        for (Pair<Integer,float[]> feature : features) {
            partialMap.clear();
            for (Map<String, PrimitiveTypeProvider> result : this.selector.getNearestNeighbourRows(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), feature.second, FIELDS[1], qc)) {

                String id = result.get(FIELDS[0]).getString();
                double score = MathHelper.getScore(result.get("ap_distance").getDouble(), MAX_DIST);
                int poseidx = result.get(FIELDS[2]).getInt();


                if (poseidx != feature.first && feature.first != POSEIDX_UNKNOWN) {
                    score /= 2.0f;
                }

                double newValue = Math.max(partialMap.get(id), score);
                partialMap.putIfAbsent(id, newValue);
            }

            for (String key : partialMap.keySet()) {
                map.adjustOrPutValue(key, partialMap.get(key)/features.size(), partialMap.get(key)/features.size());
            }
        }

        /* Add results to list. */
        for (String key : map.keySet()) {
            results.add(new StringDoublePair(key, map.get(key)));
        };

        /* Return results. */
        return results;
    }

    @Override
    public List<StringDoublePair> getSimilar(String shotId, QueryConfig qc) {
        return null;
    }

    /**
     * Processes a single segment. Extracts the mesh and persists all associated features. Segments
     * that have no mesh or an empty mesh will not be processed.
     *
     * @param sc
     */
    @Override
    public void processShot(SegmentContainer sc) {
        /* If Mesh is empty, no feature is persisted. */
        Mesh mesh = sc.getNormalizedMesh();
        if (mesh == null || mesh.isEmpty()) {
            return;
        }

        /* Extract and persist all features. */
        List<Pair<Integer,float[]>> features = this.featureVectorsFromMesh(mesh);
        for (Pair<Integer,float[]> feature : features) {
            PersistentTuple tuple = this.phandler.generateTuple(sc.getId(), new FloatVectorImpl(feature.second), feature.first);
            this.phandler.persist(tuple);
        }
    }

    /**
     * Extracts the Lightfield Fourier descriptors from a provided Mesh. The returned list contains
     * elements of which each holds a pose-index (relative to the camera-positions used by the feature module)
     * and the associated feature-vector (s).
     *
     * @param mesh Mesh for which to extract the Lightfield Fourier descriptors.
     * @return List of descriptors for mesh.
     */
    private List<Pair<Integer,float[]>> featureVectorsFromMesh(Mesh mesh) {
        /* Prepare empty list of features. */
        List<Pair<Integer,float[]>> features = new ArrayList<>(20);

        /* Retains the renderer and returns if retention fails. */
        if (!this.renderer.retain()) return features;

        /* Obtain rendered image from configured perspective. */
        for (int i=0;i<VERTICES_3D_DODECAHEDRON.length;i++) {
            this.renderer.positionCamera((float)VERTICES_3D_DODECAHEDRON[i][0], (float)VERTICES_3D_DODECAHEDRON[i][1], (float)VERTICES_3D_DODECAHEDRON[i][2]);
            this.renderer.render(mesh);
            BufferedImage image = this.renderer.obtain();
            if (image == null) {
                LOGGER.error("Could not generate feature for {} because no image could be obtained from JOGOffscreenRenderer.", this.getClass().getSimpleName());
                return features;
            }
            features.addAll(this.featureVectorsFromImage(image, i));
        }

        /* Release the rendering context. */
        this.renderer.release();

        /* Extract and persist the feature descriptors. */
        return features;
    }

    /**
     * Extracts the Lightfield Fourier descriptors from a provided BufferedImage. The returned list contains
     * elements for each identified contour of adequate size.
     *
     * @param image Image for which to extract the Lightfield Fourier descriptors.
     * @param poseidx Poseidx of the extracted image.
     * @return List of descriptors for image.
     */
    private List<Pair<Integer,float[]>> featureVectorsFromImage(BufferedImage image, int poseidx) {
        List<Contour> contours = ContourHelper.getContours(image);
        List<Pair<Integer,float[]>> features = new ArrayList<>();

        /* Select the largest, inner contour from the list of available contours. */
        for (Contour contour : contours) {
            for (List<Point2D_I32> inner : contour.internal) {
                /* Check size of selected contour. */
                if (inner.size() < SIZE * 2) continue;

                /* Calculate the descriptor for the selected contour. */
                double[] cds = ContourHelper.centroidDistance(inner, true);
                Complex[] results = this.transformer.transform(cds, TransformType.FORWARD);
                double magnitude = results[0].abs();
                float[] feature = new float[SIZE];
                for (int i = 0; i < SIZE; i++) {
                    feature[i] = (float) (results[i+1].abs() / magnitude);
                }
                features.add(new Pair<>(poseidx, MathHelper.normalizeL2(feature)));
            }
        }

        return features;
    }

    /**
     * Closes the DB access classes.
     */
    @Override
    public void finish() {
       if(this.phandler != null){
            this.phandler.close();
            this.phandler = null;
        }

        if(this.selector != null){
            this.selector.close();
            this.selector = null;
        }
    }

    /**
     *
     * @param phandlerSupply
     */
    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        this.phandler = phandlerSupply.get();
        this.phandler.open(TABLE_NAME);
        this.phandler.setFieldNames(FIELDS[0],FIELDS[1],FIELDS[2]);
    }

    /**
     *
     * @param selectorSupply
     */
    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        this.selector = selectorSupply.get();
        this.selector.open(TABLE_NAME);
    }
    /**
     *
     * @param supply
     */
    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createFeatureEntity(TABLE_NAME, false, new AttributeDefinition(FIELDS[1], AttributeDefinition.AttributeType.VECTOR), new AttributeDefinition(FIELDS[2], AttributeDefinition.AttributeType.INT));
    }

    /**
     *
     * @param supply
     */
    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(TABLE_NAME);
    }
}
