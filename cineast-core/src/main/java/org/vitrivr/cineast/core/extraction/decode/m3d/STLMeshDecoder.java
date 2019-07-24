package org.vitrivr.cineast.core.extraction.decode.m3d;

import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Decodes STereoLithography (.stl) files and returns a Mesh representation. Requires
 * JOML to work properly.
 *
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class STLMeshDecoder implements Decoder<Mesh> {
    /** Default logging facility. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Maximum number of triangles in a STL file. Larger files are discarded. */
    private static final int MAX_TRIANGLES = 5000000;

    /** HashSet containing all the mime-types supported by this ImageDecoder instance. */
    private static final Set<String> supportedFiles;
    static {
        HashSet<String> tmp = new HashSet<>();
        tmp.add("application/3d-stl");
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
     * Fetches the next piece of content of type T and returns it. This method can be safely invoked until
     * complete() returns false. From which on this method will return null.
     *
     * @return Content of type T.
     */
    @Override
    public Mesh getNext() {
        try {
            InputStream is = Files.newInputStream(this.inputFile);
            byte[] header = new byte[6];
            if (is.read(header) == 6) {
                if ((new String(header)).contains("solid ")) {
                    LOGGER.info("Found term 'solid' in header. Treating the STL file as ASCII STL file!");
                    return this.readAscii(is);
                } else {
                    LOGGER.info("Did not find term 'solid' in header. Treating the STL file as binary STL file!");
                    return this.readBinary(is, 74);
                }
            } else {
                LOGGER.warn("Could not read the first 10 bytes of the file {}. This is not a valid STL file.", this.inputFile.toString());
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("Could not decode STL file {} due to an IO exception ({})", this.inputFile.toString(), LogHelper.getStackTrace(e));
            this.complete.set(true);
            return null;
        } finally {
            this.complete.set(true);
        }
    }

    /**
     * Reads an ASCII STL file.
     *
     * @param is InputStream to read from.
     * @return Mesh
     * @throws IOException If an error occurs during reading.
     */
    private Mesh readAscii(InputStream is)  throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;

        /* Prepare empty mesh. */
        Mesh mesh = new Mesh(100,100);

        /* Prepare helper structures. */
        TObjectIntHashMap<Vector3f> vertexBuffer = new TObjectIntHashMap<>();
        int index = 0;
        int[] vertexindices = new int[3];

        while ((line = br.readLine()) != null && !line.startsWith("endsolid")) {
            line = line.trim();

            /* Detect end of STL file. */
            if (line.startsWith("endsolid")) {
              break;
            }

            /* Detect begin of facet. */
            if (line.startsWith("facet normal ")) {
                int vidx = 0;

                while ((line = br.readLine()) != null) {

                    line = line.trim(); /* Trim line. */

                    /* Detect end of facet. */
                    if (line.equals("endfacet")) {
                      break;
                    }

                    /* Detect vertex. */
                    if (line.startsWith("vertex")) {
                        String[] splitVertex = line.split("\\s+");
                        Vector3f vertex = new Vector3f(Float.parseFloat(splitVertex[1]),Float.parseFloat(splitVertex[2]), Float.parseFloat(splitVertex[3]));
                        if (!vertexBuffer.containsKey(vertex)) {
                            mesh.addVertex(vertex);
                            vertexBuffer.put(vertex, index);
                            index++;
                        }
                        vertexindices[vidx] = vertexBuffer.get(vertex);
                        vidx++;
                    }
                }

                 /* Add a new face to the Mesh. */
                mesh.addFace(new Vector3i(vertexindices[0], vertexindices[1], vertexindices[2]));
            }
        }

        /* Close the buffered reader. */
        br.close();

        /* This covers the case, where the file starts with 'solid ' but is not an ASCII file. Unfortunately, such files do exist. */
        if (mesh.numberOfVertices() == 0) {
            LOGGER.warn("The provided ASCII STL file does not seem to contain any normals or vertices. Trying to decode it as binary STL even though it was marked as being ASCII.");
            InputStream newIs = Files.newInputStream(this.inputFile);
            return this.readBinary(newIs, 80);
        } else {
            return mesh;
        }
    }

    /**
     * Reads a binary STL file.
     *
     * @param is InputStream to read from.
     * @param skip Number of bytes to skip before reading the STL file.
     * @return Mesh
     * @throws IOException If an error occurs during reading.
     */
    private Mesh readBinary(InputStream is, int skip) throws IOException {
        /* Prepare a ByteBuffer to read the rest of the STL file. */
        byte[] bytes = new byte[50];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        /* Skip the STL header! */
        is.skip(skip);

        /* Read the bytes for the size (unsigned 32 bit int, little-endian). */
        byte[] sizeBytes = new byte[4];
        is.read(sizeBytes, 0, 4);
        long triangles = ((sizeBytes[0] & 0xFF)) | ((sizeBytes[1] & 0xFF) << 8) | ((sizeBytes[2] & 0xFF) << 16) | ((sizeBytes[3] & 0xFF) << 24);

        /* TODO: Properly handle models whose triangles > MAX_TRIANGLES. */
        if (triangles <= 0) {
            LOGGER.error("The number of triangles in the Mesh seems to be smaller than zero. This STL file is probably corrupt!");
            return null;
        } else if (triangles > MAX_TRIANGLES) {
            LOGGER.error("The number of triangles in the Mesh exceeds the limit that can currently be processed by STLMeshDecoder. The Mesh will be downsampled!");
            return null;
        }

        /* Prepare Mesh. */
        Mesh mesh = new Mesh((int)triangles, (int)triangles);

        /* Prepare helper structures. */
        TObjectIntHashMap<Vector3f> vertexBuffer = new TObjectIntHashMap<>();
        int index = 0;
        int[] vertexindices = new int[3];

        /* Now add all triangles. */
        for (int i=0; i<triangles; i++) {
            /* Ready 48 bytes from the stream. */
            buffer.rewind();
            is.read(bytes);

            /* Read and ignore three floats. */
            buffer.getFloat();
            buffer.getFloat();
            buffer.getFloat();

            /* Add the vertices and the vertex-normal to the mesh. */
            for (int vidx = 0; vidx < 3; vidx++) {
                Vector3f vertex = new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
                if (!vertexBuffer.containsKey(vertex)) {
                    mesh.addVertex(vertex);
                    vertexBuffer.put(vertex, index);
                    index++;
                }
                vertexindices[vidx] = vertexBuffer.get(vertex);
            }

            /* Add a new face to the Mesh. */
            if (!mesh.addFace(new Vector3i(vertexindices[0], vertexindices[1], vertexindices[2]))) {
                LOGGER.warn("Could not add face {}/{}/{} because index points to non-existing vertex.", vertexindices[0], vertexindices[1], vertexindices[2]);
            }
        }

        /* Closes the InputStream. */
        is.close();
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
