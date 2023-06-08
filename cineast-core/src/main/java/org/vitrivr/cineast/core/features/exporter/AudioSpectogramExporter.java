package org.vitrivr.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractSegmentExporter;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;
import org.vitrivr.cineast.core.util.dsp.visualization.AudioSignalVisualizer;

/**
 * Visualizes and exporst the power spectogram (time vs. frequency vs. power) of the provided AudioSegment.
 */
public class AudioSpectogramExporter extends AbstractSegmentExporter {


  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Property names that can be used in the configuration hash map.
   */
  private static final String PROPERTY_NAME_WIDTH = "width";
  private static final String PROPERTY_NAME_HEIGHT = "height";
  private static final String PROPERTY_NAME_FORMAT = "format";


  /**
   * Width of the resulting image in pixels.
   */
  private final int width;

  /**
   * Height of the resulting image in pixels.
   */
  private final int height;

  /**
   * Output format for thumbnails. Defaults to PNG.
   */
  private final String format;

  /**
   * Default constructor
   */
  public AudioSpectogramExporter() {
    this(new HashMap<>());
  }

  /**
   * Constructor. The AudioWaveformExporter can be configured via named properties in the provided HashMap.
   * <p>
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
  public AudioSpectogramExporter(HashMap<String, String> properties) {
    super(properties);
    this.width = Integer.parseInt(properties.getOrDefault(PROPERTY_NAME_WIDTH, "800"));
    this.height = Integer.parseInt(properties.getOrDefault(PROPERTY_NAME_HEIGHT, "600"));
    this.format = properties.getOrDefault(PROPERTY_NAME_FORMAT, "JPG");
  }

  @Override
  public void exportToStream(SegmentContainer shot, OutputStream stream) {
    /* Prepare STFT and Spectrum for the segment. */
    final STFT stft = shot.getSTFT(2048, 512, new HanningWindow());
    final List<Spectrum> spectrums = stft.getPowerSpectrum();

    /* Visualize Spectrum and write it to disc. */
    try {
      BufferedImage image = AudioSignalVisualizer.visualizeSpectogram(spectrums, this.width, this.height);
      if (image != null) {
        ImageIO.write(image, format, stream);
      } else {
        LOGGER.warn("Spectrum could not be visualized!");
      }
    } catch (IOException exception) {
      LOGGER.error("A serious error occurred while writing the spectrum image! ({})", LogHelper.getStackTrace(exception));
    }
  }

  @Override
  public boolean isExportable(SegmentContainer sc) {
    return sc.getNumberOfSamples() > 0;
  }

}
