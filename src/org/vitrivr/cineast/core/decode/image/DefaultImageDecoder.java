package org.vitrivr.cineast.core.decode.image;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.decode.general.Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public class DefaultImageDecoder implements Decoder<BufferedImage> {

    /** Default logging facility. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** HashSet containing all the mime-types supported by this ImageDecoder instance. */
    private static HashSet<String> supportedFiles = new HashSet<>(Arrays.asList(ImageIO.getReaderMIMETypes()));

    /** */
    private Path input;

    /** */
    private volatile boolean hasImage;

    /**
     *
     * @param path
     */
    @Override
    public synchronized Decoder<BufferedImage> init(Path path) {
        this.input = path;
        this.hasImage = true;
        return this;
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public BufferedImage getNext() {

        synchronized(this) { this.hasImage = false; }

        try {
            InputStream is = Files.newInputStream(this.input, StandardOpenOption.READ);
            BufferedImage image = ImageIO.read(is);
            return image;
        } catch (IOException e) {
            LOGGER.log(org.apache.logging.log4j.Level.FATAL, String.format("Severe error occurred when trying to decode the image file under %s. Image will be skipped...", this.input.toString()), e);
            return null;
        }
    }

    /**
     * Returns the total number of content pieces T this decoder can return
     * for a given file.
     *
     * @return
     */
    @Override
    public int count() {
        return 1;
    }

    /**
     * Returns a list of supported files.
     *
     * @return
     */
    @Override
    public Set<String> supportedFiles() {
        return supportedFiles;
    }

    /**
     * Indicates whether or not a particular instance of the Decoder interface can
     * be re-used or not. This property can be leveraged to reduce the memory-footpring
     * of the application.
     *
     * @return True if re-use is possible, false otherwise.
     */
    @Override
    public boolean canBeReused() {
        return true;
    }

    /**
     *
     */
    @Override
    public synchronized boolean complete() {
        return !hasImage;
    }

    /**
     *
     */
    @Override
    public void close() {

    }
}
