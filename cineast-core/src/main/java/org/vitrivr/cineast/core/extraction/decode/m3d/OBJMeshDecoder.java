package org.vitrivr.cineast.core.extraction.decode.m3d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4i;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Decodes Wavefront OBJ (.obj) files and returns a Mesh representation. Requires JOML to work properly.
 *
 * Texture information is currently discarded!
 *
 * @author rgasser
 * @version 1.1
 */
public class OBJMeshDecoder implements Decoder<Mesh> {
    /** Default logging facility. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** HashSet containing all the mime-types supported by this ImageDecoder instance. */
    private static final Set<String> supportedFiles;
    static {
        HashSet<String> tmp = new HashSet<>();
        tmp.add("application/3d-obj");
        supportedFiles = Collections.unmodifiableSet(tmp);
    }

    /** Path to the input file. */
    private Path inputFile;

    /** Flag indicating whether or not the Decoder is done decoding and the content has been obtained. */
    private AtomicBoolean complete = new AtomicBoolean(false);

    /**
     * Initializes the decoder with a file. This is a necessary step before content can be retrieved from
     * the decoder by means of the getNext() method.
     *
     * @param path Path to the file that should be decoded.
     * @param decoderConfig {@link DecoderConfig} used by this {@link Decoder}.
     * @param cacheConfig The {@link CacheConfig} used by this {@link Decoder}
     * @return True if initialization was successful, false otherwise.
     */
    @Override
    public boolean init(Path path, DecoderConfig decoderConfig, CacheConfig cacheConfig) {
        this.inputFile = path;
        this.complete.set(false);
        return true;
    }

    /**
     * Fetches the next piece of content of type T and returns it. This method can be safely invoked until
     * complete() returns false. From which on this method will return null.
     *
     * @return Content of type T.
     */
    @Override
    public Mesh getNext() {
        Mesh mesh = new Mesh(100,100);
        try {
            InputStream is = Files.newInputStream(this.inputFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "v":
                        mesh.addVertex(new Vector3f(Float.parseFloat(tokens[1]),Float.parseFloat(tokens[2]),Float.parseFloat(tokens[3])));
                        break;
                    case "f":
                        boolean quad = (tokens.length == 5);
                        String[] p1 = tokens[1].split("/");
                        String[] p2 = tokens[2].split("/");
                        String[] p3 = tokens[3].split("/");
                        if (quad) {
                            String[] p4 = tokens[4].split("/");
                            Vector4i vertexIndex = new Vector4i(Integer.parseInt(p1[0])-1,Integer.parseInt(p2[0])-1,Integer.parseInt(p3[0])-1,Integer.parseInt(p4[0])-1);
                            if (!mesh.addFace(vertexIndex)) {
                                LOGGER.warn("Could not add face {}/{}/{}/{} because index points to non-existing vertex.",vertexIndex.x, vertexIndex.y, vertexIndex.z, vertexIndex.w);
                            }
                        } else {
                            Vector3i vertexIndex = new Vector3i(Integer.parseInt(p1[0])-1,Integer.parseInt(p2[0])-1,Integer.parseInt(p3[0])-1);
                            if (!mesh.addFace(vertexIndex)) {
                                LOGGER.warn("Could not add face {}/{}/{} because index points to non-existing vertex.",vertexIndex.x, vertexIndex.y, vertexIndex.z);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            br.close(); /* Closes the input stream. */
        } catch (IOException e) {
            LOGGER.error("Could not decode OBJ file {} due to an IO exception ({})", this.inputFile.toString(), LogHelper.getStackTrace(e));
            mesh = null;
        } catch (NumberFormatException e) {
            LOGGER.error("Could not decode OBJ file {} because one of the tokens could not be converted to a valid number.", this.inputFile.toString());
            mesh = null;
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error("Could not decode OBJ file {} because one of the faces points to invalid vertex indices.", this.inputFile.toString());
            mesh = null;
        } finally {
            this.complete.set(true);
        }

        return mesh;
    }

    /**
     * Returns the total number of content pieces T this decoder can return for a given file.
     *
     * @return
     */
    @Override
    public int count() {
        return 1;
    }

    /**
     * Closes the Decoder. This method should cleanup and relinquish all resources.
     * <p>
     * Note: It is unsafe to re-use a Decoder after it has been closed.
     */
    @Override
    public void close() {}

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
}
