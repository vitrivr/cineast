package org.vitrivr.cineast.core.extraction.decode.image;

import com.twelvemonkeys.image.ResampleOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;


public class DefaultImageDecoder implements Decoder<BufferedImage> {

  /* Configuration property-names and defaults for the DefaultImageDecoder. */
  private static final String CONFIG_BOUNDS_PROPERTY = "bounds";
  private static final int CONFIG_BOUNDS_DEFAULT = 1024;

  /**
   * Default logging facility.
   */
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * HashSet containing all the mime-types supported by this ImageDecoder instance.
   */
  private static final Set<String> supportedFiles = Set.of(ImageIO.getReaderMIMETypes());

  /**
   * Bounds used to rescale the image.
   */
  private int rescale_bounds = CONFIG_BOUNDS_DEFAULT;

  /**
   * Flag indicating whether or not the Decoder is done decoding and the content has been obtained.
   */
  private final AtomicBoolean complete = new AtomicBoolean(false);

  /**
   * Path to the input file.
   */
  private Path input;

  /**
   * Initializes the decoder with a file. This is a necessary step before content can be retrieved from the decoder by means of the getNext() method.
   *
   * @param path          Path to the file that should be decoded.
   * @param decoderConfig {@link DecoderConfig} used by this {@link Decoder}.
   * @param cacheConfig   The {@link CacheConfig} used by this {@link Decoder}
   * @return True if initialization was successful, false otherwise.
   */
  @Override
  public boolean init(Path path, DecoderConfig decoderConfig, CacheConfig cacheConfig) {
    this.input = path;
    this.complete.set(false);
    if (decoderConfig != null) {
      this.rescale_bounds = decoderConfig.namedAsInt(CONFIG_BOUNDS_PROPERTY, CONFIG_BOUNDS_DEFAULT);
    }
    LOGGER.debug("Supported: {}", supportedFiles);
    return true;
  }

  /**
   * Obtains and returns a result by decoding the image. The image is re-rescaled to match the bounding box defined by RESCALE_BOUNDS.
   *
   * @return BufferedImage of the decoded image file or null of decoding failed.
   */
  @Override
  public BufferedImage getNext() {
    InputStream is = null;
    BufferedImage output = null;
    BufferedImage input;
    try {
      is = Files.newInputStream(this.input, StandardOpenOption.READ);
      input = ImageIO.read(is);

      if (input != null) {
        int width = input.getWidth();
        int height = input.getHeight();
        float ratio = 0;

        if (width > rescale_bounds) {
          ratio = (float) rescale_bounds / (float) width;
          width = (int) (width * ratio);
          height = (int) (height * ratio);
        }

        if (height > rescale_bounds) {
          ratio = (float) rescale_bounds / (float) height;
          width = (int) (width * ratio);
          height = (int) (height * ratio);
        }

        final BufferedImageOp resampler = new ResampleOp(width, height, ResampleOp.FILTER_LANCZOS); // A good default filter, see class documentation for more info
        output = resampler.filter(input, null);
      }
    } catch (IOException | IllegalArgumentException e) {
      LOGGER.fatal("A severe error occurred while trying to decode the image file under '{}'. Image will be skipped...", this.input.toString());
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException e) {
        LOGGER.warn("Could not close the input stream of the image file under {}.", this.input.toString());
      }
      this.complete.set(true);
    }
    return output;
  }

  /**
   * Returns the total number of content pieces T this decoder can return for a given file.
   */
  @Override
  public int count() {
    return 1;
  }

  /**
   * Returns a list of supported files.
   */
  @Override
  public Set<String> supportedFiles() {
    return supportedFiles;
  }

  /**
   * Indicates whether or not a particular instance of the Decoder interface can be re-used or not. This property can be leveraged to reduce the memory-footpring of the application.
   *
   * @return True if re-use is possible, false otherwise.
   */
  @Override
  public boolean canBeReused() {
    return true;
  }

  /**
   * Indicates whether or not the current decoder instance is complete i.e. if there is content left that can be obtained.
   *
   * @return true if there is still content, false otherwise.
   */
  @Override
  public boolean complete() {
    return this.complete.get();
  }

  /**
   * Nothing to close!
   */
  @Override
  public void close() {
  }
}
