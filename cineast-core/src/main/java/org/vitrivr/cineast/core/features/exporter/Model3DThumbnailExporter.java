package org.vitrivr.cineast.core.features.exporter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderJob;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderWorker;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.texturemodel.EntropyOptimizer.EntopyCalculationMethod;
import org.vitrivr.cineast.core.util.texturemodel.EntropyOptimizer.EntropyOptimizerStrategy;
import org.vitrivr.cineast.core.util.texturemodel.EntropyOptimizer.ModelEntropyOptimizer;
import org.vitrivr.cineast.core.util.texturemodel.EntropyOptimizer.OptimizerOptions;


public class Model3DThumbnailExporter implements Extractor {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Property names that can be used in the configuration hash map.
   */
  private static final String PROPERTY_NAME_DESTINATION = "destination";
  private static final String PROPERTY_NAME_SIZE = "size";

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
  private final Color backgroundColor = Color.lightGray;

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
      // Get the model to generate a thumbnail for.
      IModel model = shot.getModel();
      if (model.getMaterials().size() > 0) {
        // Set options for the renderer.
        var windowOptions = new WindowOptions(400, 400) {{
          this.hideWindow = true;
        }};
        var renderOptions = new RenderOptions() {{
          this.showTextures = true;
        }};
        // Set options for the entropy optimizer.
        var opts = new OptimizerOptions() {{
          this.iterations = 100;
          this.initialViewVector = new Vector3f(0, 0, 1);
          this.method = EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA_WEIGHTED;
          this.optimizer = EntropyOptimizerStrategy.RANDOMIZED;
          this.yNegWeight = 0.7f;
          this.yPosWeight = 0.8f;
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

        // Render the model.
        var images = RenderJob.performStandardRenderJob(RenderWorker.getRenderJobQueue(), model, cameraPositions, windowOptions, renderOptions);
        assert images.size() == 4;
        // Combine the images into a single image.
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
      LOGGER.trace("Finished processing thumbnail {}.", shot.getId());
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
