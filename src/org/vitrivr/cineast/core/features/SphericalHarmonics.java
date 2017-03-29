package org.vitrivr.cineast.core.features;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.VoxelGrid;
import org.vitrivr.cineast.core.data.m3d.Voxelizer;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.math.functions.SphericalHarmonicsFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * An Extraction and Retrieval module for 3D models that leverages Spherical Harmonics as proposed in [1].
 *
 * [1] Funkhouser, T., Min, P., Kazhdan, M., Chen, J., Halderman, A., Dobkin, D., & Jacobs, D. (2003).
 *      A search engine for 3D models. ACM Trans. Graph., 22(1), 83–105. http://doi.org/10.1145/588272.588279

 * @author rgasser
 * @version 1.0
 * @created 16.02.17
 */
public class SphericalHarmonics extends AbstractFeatureModule {

    /* Size of the Voxel Grid in each of the three dimensions. */
    private static final int GRID_SIZE = 64;

    /** Voxelizer instance used with thes feature module. */
    private Voxelizer voxelizer = new Voxelizer(1.0f/GRID_SIZE);

    /**
     * Default constructor for SphericalHarmonics class.
     */
    public SphericalHarmonics() {
        super("features_sphericalharmonics", 2.0f);
    }

    /**
     *
     * @param shot
     */
    @Override
    public void processShot(SegmentContainer shot) {
        /* Get the normalized Mesh. */
        ReadableMesh mesh = shot.getNormalizedMesh();
        if (mesh == null || mesh.isEmpty()) return;

        /* Extract feature and persist it. */
        float[] feature = this.featureVectorsFromMesh(mesh, shot.getId());
        this.persist(shot.getId(), new FloatVectorImpl(feature));
    }

    /**
     *
     * @param sc
     * @param qc
     * @return
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        /* Get the normalized Mesh. */
        ReadableMesh mesh = sc.getNormalizedMesh();
        if (mesh == null || mesh.isEmpty()) new ArrayList<>();

        /* This feature module always uses the Euclidian Distance!. */
        qc.setDistance(QueryConfig.Distance.euclidean);

        /* Extract feature and persist it. */
        float[] feature = this.featureVectorsFromMesh(mesh, sc.getId());
        return this.getSimilar(feature, qc);
    }


    /**
     * Obtains the SphericalHarmonic descriptor of the Mesh. To do so, the Mesh is rasterized into a VoxelGrid of 65 x 65 x 65
     * Voxels (one *safety-voxel* per dimension to prevent ArrayIndexOutOfBounds exceptions)
     *
     * This VoxelGrid is treated as a function f(x,y,z) = 1.0 if Voxel is visible and 0.0 otherwise. The grid is
     * sampled at 7 different radii r ranging from 0.25 to 1.0, where 0.0 lies at the center of the grid and 1.0 touches
     * the bounding-box of the grid. The parameters r, ϑ, ϼ relate to the VoxelGrid as follows:
     *
     * f(x,y,z) = f(r * sin(ϑ) * cos(ϼ) * α + α, r * cos(ϑ) * α, + α, r * sin(ϑ) * sin(ϼ) * α + α)
     *
     * Where α is a constant used to translate the normalized coordinate system to the bounds of the VoxelGrid.
     *
     * Now for l = 0 to l = 4 (m = -l to +l), the projection of the function f(x,y,z) onto the SphericalHarmonic function Zlm
     *  (i.e. the integral  ∫f(ϑ,ϼ)Zlm(ϑ,ϼ)dϴdϑ) is calculated. This is done for all of the seven radii. This yields 25 descriptors per radius which results in a
     * feature vector of 7 * 25 entries.
     *
     * Depending on the model, the first components may be 0.0 because the surface of the sphere defined by the radius only
     * touches empty space (i.e the hollow interior of the model).
     *
     * @param mesh
     * @return
     */
    private float[] featureVectorsFromMesh(ReadableMesh mesh, String id) {

        final int halfGridSize = GRID_SIZE/2;
        final float radiusincrement = 0.125f; /* Increment of the radius during calculation of the descriptors. Results in 7 different radii. */
        final float angularincrement = 0.1f; /* Increment of the angles during calculation of the descriptors. */

        /* Prepares an empty array for the feature vector. */
        float[] feature = new float[7*25];

        /* Voxelizes the grid from the mesh. If the resulting grid is invisible, the method returns immediately. */
        VoxelGrid grid = this.voxelizer.voxelize(mesh, GRID_SIZE+1, GRID_SIZE+1, GRID_SIZE+1);
        if (!grid.isVisible()) return feature;


        List<List<Complex>> descriptors = new ArrayList<>();

        /*
         * Outer-loops; iterate from l=0 to 5 and m=-l to +l. For each combination, a new SphericalHarmonicsFunction is
         * created.
         */
        for (int l = 0; l<5; l++) {
            for (int m = -l; m <= l; m++) {

                final SphericalHarmonicsFunction fkt = new SphericalHarmonicsFunction(l,m);

                /*
                 * Middle-loop; Iterate over the 7 radii.
                 */
                for (int r=0; r<7;r++) {

                    /* Allocate array list for radius. */
                    if (descriptors.size() <= r) descriptors.add(new ArrayList<>());
                    List<Complex> list = descriptors.get(r);

                    /* */
                    float radius = (r + 2.0f) * radiusincrement;
                    Complex result = new Complex(0.0);

                    /*
                     * Used to calculate the projections at radius r for l and m (i.e. the integral ∫f(ϑ,ϼ)Zlm(ϑ,ϼ)dϴdϑ)
                     */
                    for (float theta=0.0f; theta<=Math.PI;theta+=angularincrement) {
                        for (float phi=0.0f; phi<=2*Math.PI;phi+=angularincrement) {
                            int x = (int)Math.floor(radius * FastMath.sin(theta) * FastMath.cos(phi) * halfGridSize) + halfGridSize;
                            int y = (int)Math.floor(radius * FastMath.cos(theta) * halfGridSize) + halfGridSize;
                            int z = (int)Math.floor(radius * FastMath.sin(theta) * FastMath.sin(phi) * halfGridSize) + halfGridSize;
                            double value = grid.isVisible(x,y,z) ? 1.0 : 0.0;
                            result = result.add(fkt.value(theta, phi).conjugate().multiply(value*angularincrement*angularincrement));
                        }
                    }

                    list.add(result);
                }
            }
        }

        /* Assembles the actual feature vector. */
        int i = 0;
        for (List<Complex> radius : descriptors) {
            for (Complex descriptor : radius) {
                feature[i] = (float)descriptor.abs();
                i++;
            }
        }

        /* Returns the normalized vector. */
        return MathHelper.normalizeL2(feature);
    }
}
