package org.vitrivr.cineast.core.decode.m3d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class STLMeshDecoder implements MeshDecoder {
    /** Default logging facility. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** HashSet containing all the mime-types supported by this ImageDecoder instance. */
    private static HashSet<String> supportedFiles = new HashSet<>();
    static {
        supportedFiles.add("application/vnd.ms-pkist");
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
     * @return Current instance of the decoder.
     */
    @Override
    public Decoder<Mesh> init(Path path, DecoderConfig config) {
        this.inputFile = path;
        this.complete.set(false);
        return this;
    }

    /**
     * Fetches the next piece of content of type T and returns it. This method can be safely invoked until
     * complete() returns false. From which on this method will return null.
     *
     * @return Content of type T.
     */
    @Override
    public Mesh getNext() {
        Mesh mesh = new Mesh();
        try {
            InputStream is = Files.newInputStream(this.inputFile);
            byte[] header = new byte[6];
            if (is.read(header) == 6) {
                if ((new String(header)).equals("solid ")) {
                    LOGGER.info("Found term 'solid' in header. Treating the STL file as ASCII STL file!");
                    this.readAscii(mesh, is);
                } else {
                    LOGGER.info("Did not find term 'solid' in header. Treating the STL file as binary STL file!");
                    this.readBinary(mesh, is, 74);
                }
            } else {
                LOGGER.warn("Could not read the first 10 bytes of the file {}. This is probably not an STL file.", this.inputFile.toString());
            }
        } catch (IOException e) {
            LOGGER.error("Could not decode STL file {} due to an IO exception ({})", this.inputFile.toString(), LogHelper.getStackTrace(e));
        } finally {
            this.complete.set(true);
        }
        return mesh;
    }

    /**
     * Reads an ASCII STL file.
     *
     * @param mesh Mesh to which normals and vertices should be added.
     * @param is InputStream to read from.
     * @throws IOException If an error occurs during reading.
     */
    private void readAscii(Mesh mesh, InputStream is)  throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        int idx = 0;
        while ((line = br.readLine()) != null && !line.startsWith("endsolid")) {
            if (line.startsWith("facet normal ")) {
                String[] splitNormal = line.split("\\s+");
                mesh.addNormal(new Vector3f(Float.parseFloat(splitNormal[2]),Float.parseFloat(splitNormal[3]), Float.parseFloat(splitNormal[4])));
                while (!(line = br.readLine()).equals("endfacet")) {
                    String[] splitVertex = line.split("\\s+");
                    if (line.startsWith("vertex")) {
                        mesh.addVertex(new Vector3f(Float.parseFloat(splitVertex[2]),Float.parseFloat(splitVertex[3]), Float.parseFloat(splitVertex[4])));
                    }
                }

                 /* Add a new face to the Mesh. */
                mesh.addFace(new Vector3i(3*idx + 1, 3*idx + 2, 3*idx + 3), new Vector3i(idx + 1, idx + 1, idx + 1));

                /* Increment index. */
                idx += 1;
            }
        }

        /* Close the buffered reader. */
        br.close();

        /* This covers the case, where the file starts with 'solid ' but is not an ASCII file. Unfortunately, such files do exist. */
        if (mesh.numberOfVertices() == 0 && mesh.numberOfNormals() == 0) {
            LOGGER.warn("The provided ASCII STL file does not seem to contain any normals or vertices. Trying to decode it as binary STL even though it was marked as being ASCII.");
            InputStream newIs = Files.newInputStream(this.inputFile);
            this.readBinary(mesh, newIs, 80);
            newIs.close();
        }
    }

    /**
     * Reads a binary STL file.
     *
     * @param mesh Mesh to which normals and vertices should be added.
     * @param is InputStream to read from.
     * @param skip Number of bytes to skip before reading the STL file.
     * @throws IOException If an error occurs during reading.
     */
    private void readBinary(Mesh mesh, InputStream is, int skip) throws IOException {
        /* Prepare a ByteBuffer to read the rest of the STL file. */
        byte[] bytes = new byte[48];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        /* Skip the STL header! */
        is.skip(skip);

        /* Read the bytes for the size (unsigned 32 bit int, little-endian). */
        byte[] sizeBytes = new byte[4];
        is.read(sizeBytes, 0, 4);
        long triangles = ((sizeBytes[0] & 0xFF)) | ((sizeBytes[1] & 0xFF) << 8) | ((sizeBytes[2] & 0xFF) << 16) | ((sizeBytes[3] & 0xFF) << 24);

        /* Now add all triangles. */
        for (int i=0; i<triangles; i++) {
            /* Ready 48 bytes from the stream. */
            buffer.rewind();
            is.read(bytes);

            /* Add the vertices and the vertex-normal to the mesh. */
            mesh.addNormal(new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()));
            mesh.addVertex(new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()));
            mesh.addVertex(new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()));
            mesh.addVertex(new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()));

            /* Add a new face to the Mesh. */
            mesh.addFace(new Vector3i(3*i + 1, 3*i + 2, 3*i + 3), new Vector3i(i + 1, i + 1, i + 1));

            /* Read 2 bytes from the stream and discard them. */
            is.read(bytes, 0, 2);
        }

        /* Closes the InputStream. */
        is.close();
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
