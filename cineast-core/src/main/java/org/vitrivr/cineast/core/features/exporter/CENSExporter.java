package org.vitrivr.cineast.core.features.exporter;

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

import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.audio.CENS;
import org.vitrivr.cineast.core.util.audio.HPCP;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;
import org.vitrivr.cineast.core.util.dsp.visualization.AudioSignalVisualizer;

/**
 * @author rgasser
 * @version 1.0
 */
public class CENSExporter implements Extractor {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Property names that can be used in the configuration hash map. */
    private static final String PROPERTY_NAME_DESTINATION = "destination";
    private static final String PROPERTY_NAME_WIDTH = "width";
    private static final String PROPERTY_NAME_HEIGHT = "height";

    /** Destination path; can be set in the CENSExporter properties. */
    private final Path destination;

    /** Width of the resulting CENS chromagram image in pixels. */
    private final int width;

    /** Height of the resulting CENS chromagram image in pixels. */
    private final int height;


    /**
     * Default constructor
     */
    public CENSExporter() {
        this(new HashMap<>());
    }

    /**
     * Constructor. The CENSExporter can be configured via named properties in the provided HashMap. Supported parameters:
     *
     * <ol>
     *      <li>destination: Path where images should be stored.</li>
     *      <li>width: Width of the image in pixels.</li>
     *      <li>height: Height of the image in pixels.</li>
     * </ol>
     *
     * @param properties HashMap containing named properties
     */
    public CENSExporter(HashMap<String, String> properties) {
        this.destination = Paths.get(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "."));
        this.width = Integer.parseInt(properties.getOrDefault(PROPERTY_NAME_WIDTH, "800"));
        this.height = Integer.parseInt(properties.getOrDefault(PROPERTY_NAME_HEIGHT, "600"));
    }

    @Override
    public void processSegment(SegmentContainer shot) {
        /* IF shot has no samples, this step is skipped. */
        if (shot.getNumberOfSamples() == 0) {
          return;
        }

        /* Prepare STFT and HPCP for the segment. */
        final Path directory = this.destination.resolve(shot.getSuperId());
        final STFT stft = shot.getSTFT(2048, 512, new HanningWindow());
        final HPCP hpcp = new HPCP();
        hpcp.addContribution(stft);

        double[][] cens = CENS.cens(hpcp, 25, 5);

        /* Visualize chromagram and write it to disc. */
        try {
            BufferedImage image = AudioSignalVisualizer.visualizeCens(cens, this.width, this.height);
            if (image != null) {
                Files.createDirectories(directory);
                ImageIO.write(image, "JPEG", directory.resolve(shot.getId() + ".jpg").toFile());
            } else {
                LOGGER.warn("CENS chromagram could not be visualized!");
            }
        } catch (IOException exception) {
            LOGGER.error("A serious error occurred while writing the CENS chromagram image! ({})", LogHelper.getStackTrace(exception));
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
