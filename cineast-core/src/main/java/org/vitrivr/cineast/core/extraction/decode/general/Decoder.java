package org.vitrivr.cineast.core.extraction.decode.general;

import org.vitrivr.cineast.core.config.DecoderConfig;

import java.nio.file.Path;
import java.util.Set;

/**
 * General interface for Decoder classes. These classes take a file as input and return one to many
 * content items of type T. Depending on what file-type this may be a single image, a set of audio samples,
 * video frames etc.
 *
 * Decoders are initialized with the init() method and closed with the close() method. Some decoders
 * may be reusable, which means that they can be re-initialized by calling init() again. Use the reusable()
 * method to determine whether or not a Decoder can be re-used.
 *
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public interface Decoder<T> extends AutoCloseable {
    /**
     * Initializes the decoder with a {@link Path}. This is a necessary step before content can be retrieved from
     * the decoder by means of the getNext() method.
     *
     * <b>Important: </b> It is not safe to call getNext() of an uninitialized decoder or a Decoder that
     * returned false upon initialization.
     *
     * @param path Path to the file that should be decoded.
     * @param config DecoderConfiguration used by the decoder.
     * @return True if initialization was successful, false otherwise.
     */
    boolean init(Path path, DecoderConfig config);

    /**
     * Closes the Decoder. This method should cleanup and relinquish all resources.
     *
     * Note: It is unsafe to re-use a Decoder after it has been closed.
     */
    @Override
    void close();

    /**
     * Fetches the next piece of content of type T and returns it. This method can be safely invoked until
     * complete() returns false. From which on this method will return null.
     *
     * @return Content of type T.
     */
    T getNext();

    /**
     * Returns the total number of content pieces T this decoder can return
     * for a given file.
     *
     * @return
     */
    int count();

    /**
     * Indicates whether or not the decoder has more content to return.
     *
     * @return True if more content can be retrieved, false otherwise.
     */
    boolean complete();

    /**
     * Returns a set of the mime/types of supported files.
     *
     * @return Set of the mime-type of file formats that are supported by the current Decoder instance.
     */
    Set<String> supportedFiles();

    /**
     * Indicates whether or not a particular instance of the Decoder interface can be re-used. If this method returns
     * true, it is save to call init() with a new file after the previous file has been fully read.
     *
     * This property can be leveraged to reduce the memory-footprint of the application.
     *
     * @return True if re-use is possible, false otherwise.
     */
    default boolean canBeReused() {
        return false;
    }
}
