package org.vitrivr.cineast.core.features;

import com.jogamp.opengl.awt.GLCanvas;
import com.twelvemonkeys.image.ImageUtil;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;
import javax.imageio.ImageIO;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.StagedFeatureModule;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;
import org.vitrivr.cineast.core.render.MeshOnlyRenderer;
import org.vitrivr.cineast.core.render.Renderer;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
import org.vitrivr.cineast.core.render.lwjgl.renderer.LWJGLOffscreenRenderer;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderActions;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderData;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderJob;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderWorker;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobType;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;
import org.vitrivr.cineast.core.util.LogHelper;

/**
 * An abstract base class for light field based feature modules as proposed by [1].
 * <p>
 * [1] Chen, D.-Y., Tian, X.-P., Shen, Y.-T., & Ouh. (2003). On Visual Similarity Based 3D Model Retrieval. In Eurographics (Vol. 22, pp. 313–318). http://doi.org/KE.2008.4730947
 */
public abstract class Lightfield extends StagedFeatureModule {

  /**
   * Size of the rendering environment.
   */
  protected final static int RENDERING_SIZE = 256;

  /**
   * Default value for an unknown pose index.
   */
  protected final static int POSEIDX_UNKNOWN = -1;

  /**
   * Camera positions used to create lightfield descriptions. - First index indicates the position-index - Second index can be used to address the x,y and z coordinates.
   * <p>
   * The array must be 1x3 at least, excess elements in the second dimension are being ignored.
   */
  private final double[][] camerapositions;


  /**
   * Offscreen rendering environment used to create Lightfield images.
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
  }


  /**
   * This method represents the first step that's executed when processing query. The associated SegmentContainer is examined and feature-vectors are being generated. The generated vectors are returned by this method together with an optional weight-vector.
   * <p>
   * <strong>Important: </strong> The weight-vector must have the same size as the feature-vectors returned by the method.
   *
   * @param sc SegmentContainer that was submitted to the feature module
   * @param qc A QueryConfig object that contains query-related configuration parameters. Can still be edited.
   * @return A pair containing a List of features and an optional weight vector.
   */
  @Override
  protected List<float[]> preprocessQuery(SegmentContainer sc, ReadableQueryConfig qc) {
    IModel model = sc.getModel();
    List<float[]> features;
    if (model == null) {
      BufferedImage image = ImageUtil.createResampled(sc.getAvgImg().getBufferedImage(), RENDERING_SIZE, RENDERING_SIZE, Image.SCALE_SMOOTH);
      features = this.featureVectorsFromImage(image, POSEIDX_UNKNOWN);
    } else {
      features = this.featureVectorsFromMesh(model);
    }
    return features;
  }

  /**
   * This method represents the last step that's executed when processing a query. A list of partial-results (DistanceElements) returned by the lookup stage is processed based on some internal method and finally converted to a list of ScoreElements. The filtered list of ScoreElements is returned by the feature module during retrieval.
   *
   * @param partialResults List of partial results returned by the lookup stage.
   * @param qc             A ReadableQueryConfig object that contains query-related configuration parameters.
   * @return List of final results. Is supposed to be de-duplicated and the number of items should not exceed the number of items per module.
   */
  @Override
  protected List<ScoreElement> postprocessQuery(List<SegmentDistanceElement> partialResults, ReadableQueryConfig qc) {
    /* Perform search for each extracted feature and adjust scores.  */
    HashMap<String, DistanceElement> map = new HashMap<>();
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
   * Merges the provided QueryConfig with the default QueryConfig enforced by the feature module.
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
   * Processes a single segment. Extracts the mesh and persists all associated features. Segments that have no mesh or an empty mesh will not be processed.
   */
  @Override
  public void processSegment(SegmentContainer sc) {

    /* If Mesh is empty, no feature is persisted. */
    IModel model = sc.getModel();
    if (model == null) {
      return;
    }

    /* Extract and persist all features. */
    List<float[]> features = this.featureVectorsFromMesh(model);
    for (float[] feature : features) {
      this.persist(sc.getId(), new FloatVectorImpl(feature));
    }
  }

  /**
   * Extracts the Lightfield Fourier descriptors from a provided Mesh. The returned list contains elements of which each holds a pose-index (relative to the camera-positions used by the feature module) and the associated feature-vector (s).
   *
   * @param model Model for which to extract the Lightfield Fourier descriptors.
   * @return List of descriptors for mesh.
   */
  protected List<float[]> featureVectorsFromMesh(IModel model) {
    /* Prepare empty list of features. */
    List<float[]> features = new ArrayList<>(20);
    var windowOptions = new WindowOptions(RENDERING_SIZE, RENDERING_SIZE) {{
      this.hideWindow = true;
    }};
    var renderOptions = new RenderOptions() {{
      this.showTextures = false;
    }};
    var camerapositions = new LinkedList<Vector3f>();
    for (double[] cameraposition : this.camerapositions) {
      camerapositions.add(new Vector3f((float)
          cameraposition[0], (float) cameraposition[1], (float) cameraposition[2]).normalize().mul(0.95f));
    }
    var images = RenderJob.performStandardRenderJob(RenderWorker.getRenderJobQueue(),
        model, camerapositions, windowOptions, renderOptions);

    var ic = 0;
    for ( var image :images){
      features.addAll(this.featureVectorsFromImage(image, ic));
      ic++;
    }
    return features;
  }


  protected abstract List<float[]> featureVectorsFromImage(BufferedImage image, int poseidx);

  public double[] positionsForPoseidx(int poseidx) {
    if (poseidx < this.camerapositions.length) {
      return this.camerapositions[poseidx];
    } else {
      return null;
    }
  }
}
