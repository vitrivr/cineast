package org.vitrivr.cineast.core.extraction.decode.m3d;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.query.containers.ModelQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.extraction.decode.general.Converter;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.util.MimeTypeHelper;

/**
 *
 * Tries to decode arbitrary 3D model files by selecting the correct decoder based on the
 * model-file's mime-type.
 *
 * @author rgasser
 * @version 1.0
 * @created 08.03.17
 */
public class ModularMeshDecoder implements MeshDecoder, Converter {
    /** Default logging facility. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** HashSet containing all the mime-types supported by this ImageDecoder instance.
     *
     * <b>Important:</b> The decoderForContenttype() method must return a Decoder<Mesh> instance
     * for all mime-types contained in this set! */
    private final static Set<String> supportedFiles;
    static {
        HashSet<String> tmp = new HashSet<>();
        tmp.add("application/3d-stl");
        tmp.add("application/3d-obj");
        tmp.add("application/3d-off");
        supportedFiles = Collections.unmodifiableSet(tmp);
    }

    /** HashMap containing cached decoder instances */
    private HashMap<String, Decoder<Mesh>> cachedDecoders = new HashMap<>();

    /** Path to the input file. */
    private Path inputFile;

    /** Flag indicating whether or not the Decoder is done decoding and the content has been obtained. */
    private AtomicBoolean complete = new AtomicBoolean(false);

    /**
     * Initializes the decoder with a file. This is a necessary step before content can be retrieved from
     * the decoder by means of the getNext() method.
     *
     * @param path   Path to the file that should be decoded.
     * @param config DecoderConfiguration used by the decoder.
     * @return True if initialization was successful, false otherwise.
     */
    @Override
    public boolean init(Path path, DecoderConfig config) {
        this.inputFile = path;
        this.complete.set(false);
        return true;
    }

    /**
     * Closes the Decoder. This method should cleanup and relinquish all resources.
     * <p>
     * Note: It is unsafe to re-use a Decoder after it has been closed.
     */
    @Override
    public void close() {}

    /**
     * Fetches the next piece of content of type T and returns it. This method can be safely invoked until
     * complete() returns false. From which on this method will return null.
     *
     * @return Content of type T.
     */
    @Override
    public Mesh getNext() {
        final String contenttype = MimeTypeHelper.getContentType(this.inputFile.toFile());

        /* Try to detach decoder from the list of cached decoders. */
        Decoder<Mesh> decoder = this.cachedDecoders.get(contenttype);

        /* If decoder is null, create a new one. */
        if (decoder == null) {
          decoder = decoderForContenttype(contenttype);
        }

        /* If decoder is still null, return an emtpy Mesh. */
        if (decoder == null) {
            LOGGER.warn("Could not find mesh decoder for provided contenttype {}.", contenttype);
            return Mesh.EMPTY;
        } else {
            this.cachedDecoders.put(contenttype, decoder);
        }

        /* Initialize the decoder and return the decoded mesh. */
        decoder.init(this.inputFile, null);
        Mesh mesh = decoder.getNext();
        this.complete.set(true);
        return mesh;
    }

    /**
     * Converts a single file to a QueryContainer.
     *
     * @param path Path the file that should be converted.
     * @return QueryContainer for the specified file.
     */
    @Override
    public QueryContainer convert(Path path) {
        final String contenttype = MimeTypeHelper.getContentType(path.toFile());

        /* Try to detach decoder from the list of cached decoders. */
        Decoder<Mesh> decoder = this.cachedDecoders.get(contenttype);

        /* If decoder is null, create a new one. */
        if (decoder == null) {
          decoder = decoderForContenttype(contenttype);
        }

        /* If decoder is still null, return an emtpy Mesh. */
        if (decoder == null) {
            LOGGER.warn("Could not find mesh decoder for provided contenttype {}.", contenttype);
            return null;
        } else {
            this.cachedDecoders.put(contenttype, decoder);
        }

        /* Initialize the decoder and return the decoded mesh. */
        decoder.init(path, null);
        Mesh mesh = decoder.getNext();
        return new ModelQueryContainer(mesh);
    }

    /**
     * Returns the total number of content pieces T this decoder can return
     * for a given file.
     *
     * @return
     */
    @Override
    public int count() {
        return 0;
    }

    /**
     * Indicates whether or not the decoder has more content to return.
     *
     * @return True if more content can be retrieved, false otherwise.
     */
    @Override
    public boolean complete() {
        return this.complete.get();
    }

    /**
     * Returns a set of the mime/types of supported files.
     *
     * @return Set of the mime-type of file formats that are supported by the current Decoder instance.
     */
    @Override
    public Set<String> supportedFiles() {
        return supportedFiles;
    }

    /**
     * Indicates whether or not a particular instance of the Decoder interface can be re-used. If this method returns
     * true, it is save to call init() with a new file after the previous file has been fully read.
     * <p>
     * This property can be leveraged to reduce the memory-footprint of the application.
     *
     * @return True if re-use is possible, false otherwise.
     */
    @Override
    public boolean canBeReused() {
        return true;
    }

    /**
     * Selects a Decoder<Mesh> implementation based on the provided content type.
     *
     * @param contenttype Mime-type for which to select a decoder.
     * @return Decoder<Mesh> or null if the mime-type is not supported.
     */
    private Decoder<Mesh> decoderForContenttype(String contenttype) {
        switch (contenttype) {
            case "application/3d-obj":
                return new OBJMeshDecoder();
            case "application/3d-stl":
                return new STLMeshDecoder();
            case "application/3d-off":
                return new OFFMeshDecoder();
            default:
                return null;
        }
    }
}
