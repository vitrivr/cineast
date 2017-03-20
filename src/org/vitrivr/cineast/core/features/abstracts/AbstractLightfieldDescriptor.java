package org.vitrivr.cineast.core.features.abstracts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;
import org.vitrivr.cineast.core.render.Renderer;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.EntityCreator;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.vitrivr.cineast.core.util.math.MathConstants.VERTICES_3D_DODECAHEDRON;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.03.17
 */
public abstract class  AbstractLightfieldDescriptor extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Field names of the entity associated with this feature module. */
    protected final static String[] FIELDS = {"id", "feature", "poseidx"};

    /** Size of the rendering environment. */
    protected final static int SIZE = 256;

    /** Default value for an unknown pose index. */
    protected final static int POSEIDX_UNKNOWN = -1;

    /** Camera positions used to create lightfield descriptions.
     *  - First index indicates the position-index
     *  - Second index can be used to address the x,y and z coordinates.
     *
     *  The array must be 1x3 at least, excess elements in the second dimension
     *  are being ignored.
     */
    protected final double[][] camerapositions;

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
        this.renderer = new JOGLOffscreenRenderer(SIZE, SIZE);
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
    protected List<Pair<Integer,float[]>> featureVectorsFromMesh(Mesh mesh) {
        /* Prepare empty list of features. */
        List<Pair<Integer,float[]>> features = new ArrayList<>(20);

        /* Retains the renderer and returns if retention fails. */
        if (!this.renderer.retain()) return features;

        /* Clears the renderer and assembles a new Mesh. */
        this.renderer.clear();
        this.renderer.assemble(mesh);

        /* Obtains rendered image from configured perspective. */
        for (int i=0;i<VERTICES_3D_DODECAHEDRON.length;i++) {
            this.renderer.positionCamera((float)this.camerapositions[i][0], (float)this.camerapositions[i][1], (float)this.camerapositions[i][2]);
            this.renderer.render();
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
