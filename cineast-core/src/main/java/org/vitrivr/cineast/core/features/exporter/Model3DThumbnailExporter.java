package org.vitrivr.cineast.core.features.exporter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.system.Configuration;
import org.vitrivr.cineast.core.data.m3d.texturemodel.ModelLoader;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
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
import org.vitrivr.cineast.core.util.texturemodel.EntopyCalculationMethod;
import org.vitrivr.cineast.core.util.texturemodel.EntropyOptimizerStrategy;
import org.vitrivr.cineast.core.util.texturemodel.ModelEntropyOptimizer;
import org.vitrivr.cineast.core.util.texturemodel.OptimizerOptions;


public class Model3DThumbnailExporter implements Extractor {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Property names that can be used in the configuration hash map.
   */
  private static final String PROPERTY_NAME_DESTINATION = "destination";
  private static final String PROPERTY_NAME_SIZE = "size";

  /**
   * List of perspective that should be rendered. Azimuth and polar angles in degree.
   */
  private static final float[][] PERSPECTIVES = {
      {0.0f, 90.0f},
      {45.0f, 135.0f},
      {-135.0f, -225.0f},
      {0.0f, -90.0f}
  };

  /**
   * Distance of camera from object.
   */
  private static final float DISTANCE = 1f; //(float) Math.sqrt(3);

  /**
   * Destination path; can be set in the AudioWaveformExporter properties.
   */
  private final Path destination;

  /**
   * Size of the resulting image in pixels (image will have dimension size x size).
   */
  private final int size;


  /**
   * Background color of the resulting image.
   */
  private Color backgroundColor = Color.lightGray;

  /**
   * Default constructor. The AudioWaveformExporter can be configured via named properties in the provided HashMap. Supported parameters:
   *
   * <ol>
   *      <li>destination: Path where images should be stored.</li>
   *      <li>size: Width of the resulting image in pixels (size x size).</li>
   * </ol>
   *
   * @param properties HashMap containing named properties
   */
  public Model3DThumbnailExporter(Map<String, String> properties) {
    this.destination = Paths.get(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "."));
    this.size = Integer.parseInt(properties.getOrDefault(PROPERTY_NAME_SIZE, "800"));
  }

  /**
   * Processes a SegmentContainer: Extracts audio-data and visualizes its waveform.
   *
   * @param shot SegmentContainer to process.
   */
  @Override
  public void processSegment(SegmentContainer shot) {
    Path directory = this.destination.resolve(shot.getSuperId());
    try {
      Files.createDirectories(directory);
      IModel model = shot.getModel();
      if (model.getMaterials().size() > 0) {

        var windowOptions = new WindowOptions(400, 400) {{
          this.hideWindow = false;
        }};
        var renderOptions = new RenderOptions() {{
          this.showTextures = true;
        }};
        var opts = new OptimizerOptions() {{
          this.iterations = 100;
          this.initialViewVector = new Vector3f(0, 0, 1);
          this.method = EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA;
          this.optimizer = EntropyOptimizerStrategy.RANDOMIZED;
        }};
        // Add a Random View, a front View an Upper Left View and an Entropy Optimized View
        var cameraPositions = new LinkedList<Vector3f>() {{
          add(new Vector3f(
              (float) (Math.random() - 0.5) * 2f,
              (float) (Math.random() - 0.5) * 2f,
              (float) (Math.random() - 0.5) * 2f)
              .normalize().mul(DISTANCE));
          add(new Vector3f(0f, 0f, 1f).normalize().mul(DISTANCE));
          add(new Vector3f(-1f, 1f, 1f).normalize().mul(DISTANCE));
          add(ModelEntropyOptimizer.getViewVectorWithMaximizedEntropy(model, opts));
        }};

        var images = RenderJob.performStandardRenderJob(RenderWorker.getRenderJobQueue(), model, cameraPositions, windowOptions, renderOptions);
        assert images.size() == 4;
        var canvas = new BufferedImage(this.size, this.size, BufferedImage.TYPE_INT_RGB);
        var graphics = canvas.getGraphics();
        graphics.setColor(this.backgroundColor);
        int sz = this.size / 2;
        var ic = 0;
        for (var partialImage : images) {
          int idx = ic % 2;
          int idy = ic < 2 ? 0 : 1;
          graphics.drawImage(partialImage, idx * sz, idy * sz, null);
          ++ic;
        }
        ImageIO.write(canvas, "JPEG", directory.resolve(shot.getId() + ".jpg").toFile());
      }
    } catch (IOException exception) {
      LOGGER.fatal("Could not export thumbnail image for model {} due to a serious IO error ({}).", shot.getId(), LogHelper.getStackTrace(exception));
    } catch (Exception exception) {
      LOGGER.error("Could not export thumbnail image for model {} because an unknown exception occurred ({}).", shot.getId(), LogHelper.getStackTrace(exception));
    } finally {
    }
  }

  @Override
  public void init(PersistencyWriterSupplier phandlerSupply) { /* Noting to init. */}

  @Override
  public void finish() { /* Nothing to finish. */}

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {/* Nothing to initialize. */}

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {/* Nothing to drop. */}

}
