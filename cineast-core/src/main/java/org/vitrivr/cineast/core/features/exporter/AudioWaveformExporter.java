package org.vitrivr.cineast.core.features.exporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.util.LogHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Visualizes and export the audio-waveform of the audio data in a given Segment.
 *
 */
public class AudioWaveformExporter implements Extractor {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Property names that can be used in the configuration hash map. */
    private static final String PROPERTY_NAME_DESTINATION = "destination";
    private static final String PROPERTY_NAME_WIDTH = "width";
    private static final String PROPERTY_NAME_HEIGHT = "height";
    private static final String PROPERTY_NAME_FORMAT = "format";

    /** Destination path; can be set in the AudioWaveformExporter properties. */
    private final Path destination;

    /** Width of the resulting image in pixels. */
    private final int width;

    /** Height of the resulting image in pixels. */
    private final int height;

    /** Output format for thumbnails. Defaults to PNG. */
    private final String format;

    /** Background color of the resulting image. */
    private final Color backgroundColor = Color.lightGray;

    /** Foreground color of the resulting image (used to draw the waveform). */
    private final Color foregroundColor = Color.darkGray;

    /**
     * Default constructor
     */
    public AudioWaveformExporter() {
        this(new HashMap<>());
    }

    /**
     * Constructor. The AudioWaveformExporter can be configured via named properties in the provided HashMap.
     *
     * Supported parameters:
     *
     * <ol>
     *      <li>destination: Path where images should be stored.</li>
     *      <li>width: Width of the image in pixels.</li>
     *      <li>height: Height of the image in pixels.</li>
     * </ol>
     *
     * @param properties HashMap containing named properties
     */
    public AudioWaveformExporter(HashMap<String, String> properties) {
        this.destination = Paths.get(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "."));
        this.width = Integer.parseInt(properties.getOrDefault(PROPERTY_NAME_WIDTH, "600"));
        this.height = Integer.parseInt(properties.getOrDefault(PROPERTY_NAME_HEIGHT, "250"));
        this.format = properties.getOrDefault(PROPERTY_NAME_FORMAT, "JPG");
    }

    /**
     * Processes a SegmentContainer: Extracts audio-data and visualizes its waveform.
     *
     * @param shot SegmentContainer to process.
     */
    @Override
    public void processSegment(SegmentContainer shot) {
        try {
            /* If shot has no samples, this step is skipped. */
            if (shot.getNumberOfSamples() == 0) {
                return;
            }

            Path directory = this.destination.resolve(shot.getSuperId());
            Files.createDirectories(directory);

            List<AudioFrame> frames = shot.getAudioFrames();

            BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = image.getGraphics();
            graphics.setColor(this.backgroundColor);
            graphics.fillRect(0,0,width,height);

            /* Determine the number of samples. */
            int samples = shot.getNumberOfSamples();

            /* Determine y position of baseline and x/y ratios. */
            int baseline = this.height / 2;
            float bin_width = (float) this.width / (float) samples;

            int s_tot = 0;

            /* Initialize bins (one for every pixel in the x direction). */
            double[] bin_max = new double[this.width];
            double[] bin_min = new double[this.width];

            for (AudioFrame frame : frames) {
                for (int s = 0; s < frame.numberOfSamples(); s++, s_tot++) {
                    int bin = (int) (bin_width * s_tot);
                    double amplitude = frame.getMeanSampleAsDouble(s);
                    bin_max[bin] = Math.max(bin_max[bin], amplitude);
                    bin_min[bin] = Math.min(bin_min[bin], amplitude);
                }
            }

            /* Draw all the bins. */
            graphics.setColor(foregroundColor);
            for (int i = 1; i < this.width; i++) {
                graphics.drawLine(i, baseline + (int) (bin_min[i] * baseline), i, baseline + (int) (bin_max[i] * baseline));
            }

            ImageIO.write(image, format, directory.resolve(shot.getId() + "." + format.toLowerCase()).toFile());
        } catch (IOException exception) {
            LOGGER.fatal("Could not export waveform image for audio segment {} due to a serious IO error ({}).", shot.getId(), LogHelper.getStackTrace(exception));
        }
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) { /* Noting to init. */}

    @Override
    public void finish() { /* Nothing to finish. */}

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {/* Nothing to initialize. */}

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {/* Nothing to drop. */}
}
