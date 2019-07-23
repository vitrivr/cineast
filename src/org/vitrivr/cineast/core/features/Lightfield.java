package org.vitrivr.cineast.core.features;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.StagedFeatureModule;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;
import org.vitrivr.cineast.core.render.Renderer;
import org.vitrivr.cineast.core.util.LogHelper;

import com.twelvemonkeys.image.ImageUtil;

/**
 * An abstract base class for light field based feature modules as proposed by [1].
 *
 * [1] Chen, D.-Y., Tian, X.-P., Shen, Y.-T., & Ouh. (2003).
 *      On Visual Similarity Based 3D Model Retrieval. In Eurographics (Vol. 22, pp. 313–318). http://doi.org/KE.2008.4730947
 *
 * @author rgasser
 * @version 1.0
 * @created 17.03.17
 */
public abstract class Lightfield extends StagedFeatureModule {

    /** Size of the rendering environment. */
    protected final static int RENDERING_SIZE = 256;

    /** Default value for an unknown pose index. */
    protected final static int POSEIDX_UNKNOWN = -1;

    /**
     *  Camera positions used to create lightfield descriptions.
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
    protected Lightfield(String tableName, float maxDist, int vectorLength, double[][] camerapositions) {
        super(tableName, maxDist, vectorLength);
        if (camerapositions.length == 0) {
          throw new IllegalArgumentException("You must specify at least one camera position!");
        }
        for (double[] position : camerapositions) {
            if (position.length < 3) {
              throw new IllegalArgumentException("Each position must have at least three coordinates.");
            }
        }
        this.camerapositions = camerapositions;

        /*
         * Instantiate JOGLOffscreenRenderer.
         * Handle the case where it cannot be created due to missing OpenGL support.
         */
        JOGLOffscreenRenderer renderer = null;
        try {
            renderer = new JOGLOffscreenRenderer(RENDERING_SIZE, RENDERING_SIZE);
        } catch (Exception exception) {
            LOGGER.error("Could not instantiate JOGLOffscreenRenderer! This instance of {} will not create any results or features!", this.getClass().getSimpleName());
        } finally {
            this.renderer = renderer;
        }
    }


    /**
     * This method represents the first step that's executed when processing query. The associated SegmentContainer is
     * examined and feature-vectors are being generated. The generated vectors are returned by this method together with an
     * optional weight-vector.
     * <p>
     * <strong>Important: </strong> The weight-vector must have the same size as the feature-vectors returned by the method.
     *
     * @param sc SegmentContainer that was submitted to the feature module
     * @param qc A QueryConfig object that contains query-related configuration parameters. Can still be edited.
     * @return A pair containing a List of features and an optional weight vector.
     */
    @Override
    protected List<float[]> preprocessQuery(SegmentContainer sc, ReadableQueryConfig qc) {
        /* Check if renderer could be initialised. */
        if (this.renderer == null) {
            LOGGER.error("No renderer found. {} does not return any results.", this.getClass().getSimpleName());
            return new ArrayList<>(0);
        }

        /* Extract features from either the provided Mesh (1) or image (2). */
        ReadableMesh mesh = sc.getNormalizedMesh();
        List<float[]> features;
        if (mesh.isEmpty()) {
            BufferedImage image = ImageUtil.createResampled(sc.getAvgImg().getBufferedImage(), RENDERING_SIZE, RENDERING_SIZE, Image.SCALE_SMOOTH);
            features = this.featureVectorsFromImage(image,POSEIDX_UNKNOWN);
        } else {
            features = this.featureVectorsFromMesh(mesh);
        }

        return features;
    }

    /**
     * This method represents the last step that's executed when processing a query. A list of partial-results (DistanceElements) returned by
     * the lookup stage is processed based on some internal method and finally converted to a list of ScoreElements. The filtered list of
     * ScoreElements is returned by the feature module during retrieval.
     *
     * @param partialResults List of partial results returned by the lookup stage.
     * @param qc             A ReadableQueryConfig object that contains query-related configuration parameters.
     * @return List of final results. Is supposed to be de-duplicated and the number of items should not exceed the number of items per module.
     */
    @Override
    protected List<ScoreElement> postprocessQuery(List<SegmentDistanceElement> partialResults, ReadableQueryConfig qc) {
        /* Perform search for each extracted feature and adjust scores.  */
        HashMap<String,DistanceElement> map = new HashMap<>();
        for (DistanceElement result : partialResults) {
            map.merge(result.getId(), result, (v1, v2) -> {
                if (v1.getDistance() < v2.getDistance()) {
                    return v1;
                } else {
                    return v2;
                }
            });
        }

        /* Add results to list and return list of results. */
        final CorrespondenceFunction correspondence = qc.getCorrespondenceFunction().orElse(this.correspondence);
        return ScoreElement.filterMaximumScores(map.entrySet().stream().map((e) -> e.getValue().toScore(correspondence)));
    }

    /**
     * Merges the provided QueryConfig with the default QueryConfig enforced by the
     * feature module.
     *
     * @param qc QueryConfig provided by the caller of the feature module.
     * @return Modified QueryConfig.
     */
    @Override
    protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
        return new QueryConfig(qc)
                .setCorrespondenceFunctionIfEmpty(this.correspondence)
                .setDistanceIfEmpty(QueryConfig.Distance.euclidean);
    }

    /**
     * Processes a single segment. Extracts the mesh and persists all associated features. Segments
     * that have no mesh or an empty mesh will not be processed.
     *
     * @param sc
     */
    @Override
    public void processSegment(SegmentContainer sc) {
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
        List<float[]> features = this.featureVectorsFromMesh(mesh);
        for (float[] feature : features) {
            this.persist(sc.getId(), new FloatVectorImpl(feature));
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
    protected List<float[]> featureVectorsFromMesh(ReadableMesh mesh) {
        /* Prepare empty list of features. */
        List<float[]> features = new ArrayList<>(20);

        /* Retains the renderer and returns if retention fails. */
        if (!this.renderer.retain()) {
          return features;
        }

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
    protected abstract List<float[]> featureVectorsFromImage(BufferedImage image, int poseidx);

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
}
