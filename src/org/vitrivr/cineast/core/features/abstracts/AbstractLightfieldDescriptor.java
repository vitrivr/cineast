package org.vitrivr.cineast.core.features.abstracts;

import com.twelvemonkeys.image.ImageUtil;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;
import org.vitrivr.cineast.core.render.Renderer;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MathHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.03.17
 */
public abstract class  AbstractLightfieldDescriptor extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Field names of the entity associated with this feature module. */
    private final static String[] FIELDS = {"id", "feature", "poseidx"};

    /** Size of the rendering environment. */
    protected final static int SIZE = 256;

    /** Default value for an unknown pose index. */
    private final static int POSEIDX_UNKNOWN = -1;

    /** Camera positions used to create lightfield descriptions.
     *  - First index indicates the position-index
     *  - Second index can be used to address the x,y and z coordinates.
     *
     *  The array must be 1x3 at least, excess elements in the second dimension
     *  are being ignored.
     */
    private final double[][] camerapositions;

    /** Offscreen rendering environment used to create Lightfield images. */
    private final Renderer renderer;

    /**
     *
     * @param tableName
     * @param maxDist
     * @param camerapositions
     */
    protected AbstractLightfieldDescriptor(String tableName, float maxDist, double[][] camerapositions) {
        super(tableName, maxDist);
        if (camerapositions.length == 0) throw new IllegalArgumentException("You must specify at least one camera position!");
        for (double[] position : camerapositions) {
            if (position.length < 3) throw new IllegalArgumentException("Each position must have at least three coordinates.");
        }
        this.camerapositions = camerapositions;

        /*
         * Instantiate JOGLOffscreenRenderer.
         * Handle the case where it cannot be created due to missing OpenGL support.
         */
        JOGLOffscreenRenderer renderer = null;
        try {
            renderer = new JOGLOffscreenRenderer(SIZE, SIZE);
        } catch (Exception exception) {
            LOGGER.error("Could not instantiate JOGLOffscreenRenderer! This instance of {} will not create any results or features!", this.getClass().getSimpleName());
        } finally {
            this.renderer = renderer;
        }
    }

    /**
     * Processes a SegmentContainer that has been transmitted for retrieval.
     *
     * @param sc SegmentContainer
     * @param qc QueryConfiguration
     */
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        /* Initialize list with empty results. */
        List<StringDoublePair> results = new ArrayList<>();

        /* Check for renderer. */
        if (this.renderer == null) {
            LOGGER.error("No renderer found. {} does not return any results.", this.getClass().getSimpleName());
            return results;
        }

        /* Initialize helper data structures. */
        TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), 0.5f, 0.0f);
        List<Pair<Integer,float[]>> features;

        /* Extract features from either the provided Mesh (1) or image (2). */
        ReadableMesh mesh = sc.getNormalizedMesh();
        if (mesh.isEmpty()) {
            BufferedImage image = ImageUtil.createResampled(sc.getAvgImg().getBufferedImage(), SIZE, SIZE, Image.SCALE_SMOOTH);
            features = this.featureVectorsFromImage(image,POSEIDX_UNKNOWN);
        } else {
            features = this.featureVectorsFromMesh(mesh);
        }

        /* Perform search for each extracted feature and adjust scores. */
        for (Pair<Integer,float[]> feature : features) {
            for (Map<String, PrimitiveTypeProvider> result : this.selector.getNearestNeighbourRows(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), feature.second, FIELDS[1], qc)) {
                /* Extract fields from resultset. */
                final String id = result.get(FIELDS[0]).getString();
                final int poseidx = result.get(FIELDS[2]).getInt();
                final double distance = result.get("ap_distance").getDouble();
                final double penalty = (poseidx == feature.first) ? 0.0 : 0.1;

                /* Adjust distance in map. */
                if (map.containsKey(id)) {
                    double current = map.get(id);
                    map.adjustValue(id, (distance-current)/2.0 + penalty);
                } else {
                    map.put(id, distance + penalty);
                }
            }
        }

        /* Add results to list and return list of results. */
        map.forEachEntry((key, value) -> results.add(new StringDoublePair(key, MathHelper.getScore(value, this.maxDist))));
        return results;
    }

    /**
     * Processes a single segment. Extracts the mesh and persists all associated features. Segments
     * that have no mesh or an empty mesh will not be processed.
     *
     * @param sc
     */
    @Override
    public void processShot(SegmentContainer sc) {
        /* Check for renderer. */
        if (this.renderer == null) {
            LOGGER.error("No renderer found! {} does not create any features.", this.getClass().getSimpleName());
            return;
        }

        /* If Mesh is empty, no feature is persisted. */
        ReadableMesh mesh = sc.getNormalizedMesh();
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
    protected List<Pair<Integer,float[]>> featureVectorsFromMesh(ReadableMesh mesh) {
        /* Prepare empty list of features. */
        List<Pair<Integer,float[]>> features = new ArrayList<>(20);

        /* Retains the renderer and returns if retention fails. */
        if (!this.renderer.retain()) return features;

        /* Everything happens in the try-catch block so as to make sure, that if any exception occurs,
         * the renderer is released again.
         */
        try {
            /* Clears the renderer and assembles a new Mesh. */
            this.renderer.clear();
            this.renderer.assemble(mesh);

            /* Obtains rendered image from configured perspective. */
            for (int i = 0; i < this.camerapositions.length; i++) {
                /* Adjust the camera and render the image. */
                this.renderer.positionCamera((float) this.camerapositions[i][0], (float) this.camerapositions[i][1], (float) this.camerapositions[i][2]);
                this.renderer.render();
                BufferedImage image = this.renderer.obtain();
                if (image == null) {
                    LOGGER.error("Could not generate feature for {} because no image could be obtained from JOGOffscreenRenderer.", this.getClass().getSimpleName());
                    return features;
                }
                features.addAll(this.featureVectorsFromImage(image, i));
            }

        } catch (Exception exception) {
            LOGGER.error("Could not generate feature for {} because an unknown exception occurred ({}).", this.getClass().getSimpleName(), LogHelper.getStackTrace(exception));
        } finally {
            /* Release the rendering context. */
            this.renderer.release();
        }

        /* Extract and persist the feature descriptors. */
        return features;
    }

    /**
     *
     * @param image
     * @param poseidx
     * @return
     */
    protected abstract List<Pair<Integer,float[]>> featureVectorsFromImage(BufferedImage image, int poseidx);

    /**
     *
     * @param poseidx
     * @return
     */
    public double[] positionsForPoseidx(int poseidx) {
        if (poseidx < this.camerapositions.length) {
            return this.camerapositions[poseidx];
        } else {
            return null;
        }
    }

    /**
     *
     * @param phandlerSupply
     */
    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        this.phandler = phandlerSupply.get();
        this.phandler.open(this.tableName);
        this.phandler.setFieldNames(FIELDS[0],FIELDS[1],FIELDS[2]);
    }

    /**
     *
     * @param supply
     */
    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createFeatureEntity(this.tableName, false, new AttributeDefinition(FIELDS[1], AttributeDefinition.AttributeType.VECTOR), new AttributeDefinition(FIELDS[2], AttributeDefinition.AttributeType.INT));
    }
}
