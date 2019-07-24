package org.vitrivr.cineast.core.features.exporter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.data.m3d.WritableMesh;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.mesh.MeshColoringUtil;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.03.17
 */
public class Model3DThumbnailExporter implements Extractor {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Property names that can be used in the configuration hash map. */
    private static final String PROPERTY_NAME_DESTINATION = "destination";
    private static final String PROPERTY_NAME_SIZE = "size";

    /** List of perspective that should be rendered. Azimuth and polar angles in degree. */
    private static final float[][] PERSPECTIVES = {
            {0.0f,90.0f},
            {45.0f,135.0f},
            {-135.0f,-225.0f},
            {0.0f,-90.0f}
    };

    /** Distance of camera from object. */
    private static final float DISTANCE = 2.0f;

    /** Destination path; can be set in the AudioWaveformExporter properties. */
    private final Path destination;

    /** Size of the resulting image in pixels (image will have dimension size x size). */
    private final int size;

    /** Offscreen rendering context. */
    private final JOGLOffscreenRenderer renderer;

    /** Background color of the resulting image. */
    private Color backgroundColor = Color.lightGray;

    /**
     * Default constructor. The AudioWaveformExporter can be configured via named properties
     * in the provided HashMap. Supported parameters:
     *
     * <ol>
     *      <li>destination: Path where images should be stored.</li>
     *      <li>size: Width of the resulting image in pixels (size x size).</li>
     * </ol>
     *
     * @param properties HashMap containing named properties
     */
    public Model3DThumbnailExporter(HashMap<String, String> properties) {
        this.destination = Paths.get(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "."));
        this.size = Integer.parseInt(properties.getOrDefault(PROPERTY_NAME_SIZE, "800"));
        this.renderer = new JOGLOffscreenRenderer(this.size/2, this.size/2);
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
            WritableMesh mesh = shot.copyNormalizedMesh();

            if (!mesh.isEmpty()) {
                /* Colors the mesh. */
                MeshColoringUtil.normalColoring(mesh);

                BufferedImage buffer = null;
                BufferedImage image = new BufferedImage(this.size, this.size, BufferedImage.TYPE_INT_RGB);
                Graphics graphics = image.getGraphics();


                if (this.renderer.retain()) {
                    this.renderer.clear(this.backgroundColor);
                    this.renderer.assemble(mesh);

                    for (int i=0; i<4; i++) {
                        this.renderer.positionCameraPolar( DISTANCE, PERSPECTIVES[i][0], PERSPECTIVES[i][1], 0.0, 0.0, 0.0);
                        this.renderer.render();
                        buffer = this.renderer.obtain();

                        int idx = i % 2;
                        int idy = i < 2 ? 0 : 1;
                        int sz = this.size/2;

                        graphics.drawImage(buffer, idx * sz, idy*sz, null);
                    }
                } else {
                    LOGGER.error("Could not export thumbnail image for model {} because renderer could not be retained by current thread.", shot.getId());
                }
                ImageIO.write(image, "JPEG", directory.resolve(shot.getId() + ".jpg").toFile());
            }
        } catch (IOException exception) {
            LOGGER.fatal("Could not export thumbnail image for model {} due to a serious IO error ({}).", shot.getId(), LogHelper.getStackTrace(exception));
        } catch (Exception exception) {
            LOGGER.error("Could not export thumbnail image for model {} because an unknown exception occurred ({}).", shot.getId(), LogHelper.getStackTrace(exception));
        } finally {
            this.renderer.release();
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
