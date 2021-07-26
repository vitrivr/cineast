package org.vitrivr.cineast.core.features;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.VoxelGrid;
import org.vitrivr.cineast.core.data.m3d.Voxelizer;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.StagedFeatureModule;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.math.functions.SphericalHarmonicsFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * An Extraction and Retrieval module for 3D models that leverages Spherical Harmonics as proposed in [1].
 *
 * [1] Funkhouser, T., Min, P., Kazhdan, M., Chen, J., Halderman, A., Dobkin, D., & Jacobs, D. (2003).
 *      A search engine for 3D models. ACM Trans. Graph., 22(1), 83–105. http://doi.org/10.1145/588272.588279

  */
public abstract class SphericalHarmonics extends StagedFeatureModule {
    /** Voxelizer instance used with thes feature module. */
    private final Voxelizer voxelizer;

    /* Size of the Voxel Grid in each of the three dimensions. */
    private final int grid_size;

    /* The maximum harmonic to consider for the feature vector. */
    private final int min_l;

    /* The maximum harmonic to consider for the feature vector. */
    private final int max_l;

    /**
     * Constructor for SphericalHarmonics feature module.
     *
     * @param name Name of the entity for storage.
     * @param grid_size Size of the Voxel-Grid
     * @param max_l Maximum harmonic l to consider for feature vector.
     */
    public SphericalHarmonics(String name, int grid_size, int min_l, int max_l) {
        super(name, 2.0f, (grid_size/2 - 10)*(SphericalHarmonicsFunction.numberOfCoefficients(max_l, true) - SphericalHarmonicsFunction.numberOfCoefficients(min_l-1, true)));
        this.grid_size = grid_size;
        this.min_l = min_l;
        this.max_l = max_l;
        this.voxelizer = new Voxelizer(2.0f/grid_size);
    }

    /**
     *
     * @param shot
     */
    @Override
    public void processSegment(SegmentContainer shot) {
        /* Get the normalized Mesh. */
        ReadableMesh mesh = shot.getNormalizedMesh();
        if (mesh == null || mesh.isEmpty()) {
          return;
        }

        /* Extract feature and persist it. */
        float[] feature = this.featureVectorFromMesh(mesh);
        this.persist(shot.getId(), new FloatVectorImpl(feature));
    }

    /**
     * This method represents the first step that's executed when processing query. The associated SegmentContainer is
     * examined and feature-vectors are being generated. The generated vectors are returned by this method together with an
     * optional weight-vector.
     *
     * <strong>Important: </strong> The weight-vector must have the same size as the feature-vectors returned by the method.
     *
     * @param sc SegmentContainer that was submitted to the feature module.
     * @param qc A QueryConfig object that contains query-related configuration parameters. Can still be edited.
     * @return List of feature vectors for lookup.
     */
    @Override
    protected List<float[]> preprocessQuery(SegmentContainer sc, ReadableQueryConfig qc) {
        /* Initialize list of features. */
        List<float[]> features = new ArrayList<>();

        /* Get the normalized Mesh. */
        ReadableMesh mesh = sc.getNormalizedMesh();
        if (mesh == null || mesh.isEmpty()) {
          return features;
        }

        /* Extract feature and persist it. */
        features.add(this.featureVectorFromMesh(mesh));
        return features;
    }

    /**
     * This method represents the last step that's executed when processing a query. A list of partial-results (DistanceElements) returned by
     * the lookup stage is processed based on some internal method and finally converted to a list of ScoreElements. The filtered list of
     * ScoreElements is returned by the feature module during retrieval.
     *
     * @param partialResults List of partial results returned by the lookup stage.
     * @param qc A ReadableQueryConfig object that contains query-related configuration parameters.
     * @return List of final results. Is supposed to be de-duplicated and the number of items should not exceed the number of items per module.
     */
    @Override
    protected List<ScoreElement> postprocessQuery(List<SegmentDistanceElement> partialResults, ReadableQueryConfig qc) {
        final CorrespondenceFunction correspondence = qc.getCorrespondenceFunction().orElse(this.correspondence);
        return ScoreElement.filterMaximumScores(partialResults.stream().map(v -> v.toScore(correspondence)));
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
    private float[] featureVectorFromMesh(ReadableMesh mesh) {
        final float increment = 0.1f; /* Increment of the angles during calculation of the descriptors. */
        final int cap = 10; /* Cap on R (i.e. radii up to R-cap are considered). */
        final int R = this.grid_size/2;
        final int numberOfCoefficients = SphericalHarmonicsFunction.numberOfCoefficients(this.max_l, true) - SphericalHarmonicsFunction.numberOfCoefficients(this.min_l-1, true);

        /* Prepares an empty array for the feature vector. */
        float[] feature = new float[(R-cap)*numberOfCoefficients];

        /* Voxelizes the grid from the mesh. If the resulting grid is invisible, the method returns immediately. */
        VoxelGrid grid = this.voxelizer.voxelize(mesh, this.grid_size + 1, this.grid_size + 1, this.grid_size + 1);
        if (!grid.isVisible()) {
          return feature;
        }

        List<List<Complex>> descriptors = new ArrayList<>();

        /*
         * Outer-loops; iterate from l=0 to 5 and m=-l to +l. For each combination, a new SphericalHarmonicsFunction is
         * created.
         */
        for (int l = this.min_l; l<=this.max_l; l++) {
            for (int m = 0; m <= l; m++) {

                final SphericalHarmonicsFunction fkt = new SphericalHarmonicsFunction(l,m);

                /*
                 * Middle-loop; Iterate over the 7 radii.
                 */
                for (int r=0;r<R-cap;r++) {
                    /* Allocate array list for radius. */
                    if (descriptors.size() <= r) {
                      descriptors.add(new ArrayList<>());
                    }
                    List<Complex> list = descriptors.get(r);

                    Complex result = new Complex(0.0);

                    /*
                     * Used to calculate the projections at radius r for l and m (i.e. the integral ∫f(ϑ,ϼ)Zlm(ϑ,ϼ)dϴdϑ)
                     */
                    for (float theta=0.0f; theta<=2*Math.PI;theta+=increment) {
                        for (float phi=0.0f; phi<=Math.PI;phi+=increment) {
                            int x = (int)((r+1) * FastMath.sin(theta) * FastMath.cos(phi)) + R;
                            int y = (int)((r+1) * FastMath.cos(theta)) + R;
                            int z = (int)((r+1) * FastMath.sin(theta) * FastMath.sin(phi)) + R;

                            if (grid.isVisible(x,y,z)) {
                                result = result.add(fkt.value(theta, phi).conjugate().multiply(increment*increment));
                            }
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
