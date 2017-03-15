package org.vitrivr.cineast.core.features.exporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.mesh.MeshColoringUtil;
import org.vitrivr.cineast.core.util.mesh.MeshTransformUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.03.17
 */
public class Model3DThumbnailExporter implements Extractor {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Property names that can be used in the configuration hash map. */
    private static final String PROPERTY_NAME_DESTINATION = "destination";
    private static final String PROPERTY_NAME_WIDTH = "width";
    private static final String PROPERTY_NAME_HEIGHT = "height";
    private static final String PROPERTY_NAME_DISTANCE = "distance";
    private static final String PROPERTY_NAME_POLAR = "polar";
    private static final String PROPERTY_NAME_AZIMUT = "azimut";

    /** Destination path; can be set in the AudioWaveformExporter properties. */
    private Path destination = Paths.get(Config.sharedConfig().getExtractor().getOutputLocation().toString());

    /** Width of the resulting image in pixels. */
    private int width = 800;

    /** Height of the resulting image in pixels. */
    private int height = 800;

    /** */
    private float distance = 2.0f;

    /** */
    private float polar = 20.0f;

    /** */
    private float azimut = 40.0f;

    /** */
    private final JOGLOffscreenRenderer renderer;

    /** Background color of the resulting image. */
    private Color backgroundColor = Color.lightGray;

    /** Foreground color of the resulting image (used to draw the waveform). */
    private Color foregroundColor = Color.darkGray;

    /**
     * Default constructor. The AudioWaveformExporter can be configured via named properties
     * in the provided HashMap. Supported parameters:
     *
     * <ol>
     *      <li>destination: Path where images should be stored.</li>
     *      <li>width: Width of the image in pixels.</li>
     *      <li>height: Height of the image in pixels.</li>
     * </ol>
     *
     * @param properties HashMap containing named properties
     */
    public Model3DThumbnailExporter(HashMap<String, String> properties) {
        if (properties.containsKey(PROPERTY_NAME_DESTINATION)) {
            this.destination = Paths.get(properties.get(PROPERTY_NAME_DESTINATION));
        }

        if (properties.containsKey(PROPERTY_NAME_WIDTH)) {
            this.width = Integer.parseInt(properties.get(PROPERTY_NAME_WIDTH));
        }

        if (properties.containsKey(PROPERTY_NAME_HEIGHT)) {
            this.height = Integer.parseInt(properties.get(PROPERTY_NAME_HEIGHT));
        }

        if (properties.containsKey(PROPERTY_NAME_DISTANCE)) {
            this.distance = Float.parseFloat(properties.get(PROPERTY_NAME_DISTANCE));
        }

        if (properties.containsKey(PROPERTY_NAME_POLAR)) {
            this.polar = Float.parseFloat(properties.get(PROPERTY_NAME_POLAR));
        }

        if (properties.containsKey(PROPERTY_NAME_AZIMUT)) {
            this.azimut = Float.parseFloat(properties.get(PROPERTY_NAME_AZIMUT));
        }

        this.renderer = new JOGLOffscreenRenderer(this.width, this.height);
    }

    /**
     * Processes a SegmentContainer: Extracts audio-data and visualizes its waveform.
     *
     * @param shot SegmentContainer to process.
     */
    @Override
    public void processShot(SegmentContainer shot) {
        Path directory = this.destination.resolve(shot.getSuperId());
        try {
            Files.createDirectories(directory);
            Mesh mesh = shot.copyNormalizedMesh();

            if (!mesh.isEmpty()) {
                /* Colors the mesh. */
                MeshColoringUtil.color(mesh);

                BufferedImage image = null;
                if (this.renderer.retain()) {
                    this.renderer.positionCameraPolar( this.distance, this.polar, this.azimut, 0.0, 0.0, 0.0);
                    this.renderer.render(mesh);
                    image = this.renderer.obtain();
                    this.renderer.release();
                } else {
                    LOGGER.error("Could not export thumbnail image for model {} because renderer could not be retained by current thread.", shot.getId());
                }
                if (image != null) ImageIO.write(image, "JPEG", directory.resolve(shot.getId() + ".jpg").toFile());
            }
        } catch (IOException exception) {
            LOGGER.fatal("Could not export thumbnail image for model {} due to a serious IO error ({}).", shot.getId(), LogHelper.getStackTrace(exception));
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
