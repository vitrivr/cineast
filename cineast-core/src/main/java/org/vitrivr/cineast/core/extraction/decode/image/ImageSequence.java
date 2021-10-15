package org.vitrivr.cineast.core.extraction.decode.image;

import com.twelvemonkeys.image.ResampleOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Represents a media object of type {@link MediaType.IMAGE_SEQUENCE}, i.e. a sequence of images contained in a folder.
 * This class is merely an internal abstraction of that type and the content it represents. Its sole purpose is to
 * provide lazy access to the images contained in such a sequence during the extraction process.
 *
 */
public final class ImageSequence {
    /* Configuration property-names and defaults for the DefaultImageDecoder. */
    private static final String CONFIG_BOUNDS_PROPERTY = "bounds";
    private static final int CONFIG_BOUNDS_DEFAULT = 1024;

    /** Default logging facility. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** HashSet containing all the mime-types supported by this ImageDecoder instance. */
    public static final Set<String> SUPPORTED_FILES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ImageIO.getReaderMIMETypes())));

    /** Bounds used to rescale the image. */
    private final int rescale_bounds;

    /** List of suppliers for {@link BufferedImage}s. */
    private final Queue<Supplier<Pair<Path,Optional<BufferedImage>>>> images = new ConcurrentLinkedQueue<>();

    /**
     * Default constructor for {@link ImageSequence}.
     *
     * @param config {@link DecoderConfig} to use.
     */
    public ImageSequence(DecoderConfig config) {
        if (config != null) {
            this.rescale_bounds = config.namedAsInt(CONFIG_BOUNDS_PROPERTY, CONFIG_BOUNDS_DEFAULT);
        } else {
            this.rescale_bounds = CONFIG_BOUNDS_DEFAULT;
        }
    }

    /**
     * Adds a new {@link Path} to an image to this {@link ImageSequence}
     *
     * @param path The {@link Path} to add.
     */
    public void add(Path path) {
        this.images.add(() -> {
            try (final InputStream is = Files.newInputStream(path, StandardOpenOption.READ);){
                final BufferedImage input = ImageIO.read(is);
                if (input != null) {
                    int width = input.getWidth();
                    int height = input.getHeight();
                    float ratio;

                    if (width > rescale_bounds) {
                        ratio = (float)rescale_bounds/(float)width;
                        width = (int)(width * ratio);
                        height = (int)(height * ratio);
                    }

                    if (height > rescale_bounds) {
                        ratio = (float)rescale_bounds/(float)height;
                        width = (int)(width * ratio);
                        height = (int)(height * ratio);
                    }

                    final BufferedImageOp resampler = new ResampleOp(width, height, ResampleOp.FILTER_LANCZOS); // A good default filter, see class documentation for more info
                    return new Pair<>(path, Optional.of(resampler.filter(input, null)));
                }
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.fatal("A severe error occurred while trying to decode the image file under '{}'. Image will be skipped...", path.toString());
            }
            return new Pair<>(path, Optional.empty());
        });
    }

    /**
     * Pops and returns the next {@link BufferedImage}. Since the {@link BufferedImage}s are calculated lazily, invocation of this method can take a while.
     *
     * @return Next {@link BufferedImage}
     */
    public Pair<Path,Optional<BufferedImage>> pop() {
        final Supplier<Pair<Path,Optional<BufferedImage>>> supplier = this.images.poll();
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }
}
