package org.vitrivr.cineast.core.decode.image;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.util.LogHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    /** Path to the input file. */
    private Path input;

    /** Flag indicating whether or not the Decoder is done decoding and the content has been obtained. */
    private volatile boolean complete;

    /**
     * Default constructor.
     * 
     * @param path
     */
    @Override
    public synchronized Decoder<BufferedImage> init(Path path) {
        this.input = path;
        this.complete = false;
        return this;
    }

    /**
     * Obtains and returns a result by decoding the image.
     *
     * @return BufferedImage of the decoded image file or null of decoding failed.
     */
    @Override
    public BufferedImage getNext() {
        synchronized(this) { this.complete = true; }
        InputStream is = null;
        BufferedImage image = null;
        try {
            is = Files.newInputStream(this.input, StandardOpenOption.READ);
            image = ImageIO.read(is);
        } catch (IOException e) {
            LOGGER.fatal("Severe error occurred when trying to decode the image file under {}. Image will be skipped...", this.input.toString());
            LogHelper.getStackTrace(e);
            return null;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                LOGGER.warn("Could not close the input stream of the image file under {}.", this.input.toString());
                LogHelper.getStackTrace(e);
            }
        }

        return image;
    }

    /**
     * Returns the total number of content pieces T this decoder can return
     * for a given file.
     *
     * @return
     */
    @Override
    public synchronized int count() {
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
     * Indicates whether or not the current decoder instance is complete i.e. if there is
     * content left that can be obtained.
     *
     * @return true if there is still content, false otherwise.
     */
    @Override
    public synchronized boolean complete() {
        return this.complete;
    }

    /**
     * Nothing to close!
     */
    @Override
    public void close() {

    }
}
