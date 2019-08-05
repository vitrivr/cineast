package org.vitrivr.cineast.core.extraction.decode.m3d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4i;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.config.ImageCacheConfig;
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
 * Decodes OFF (.off) files as defined by [1] and returns a Mesh representation. Requires JOML to work properly.
 *
 * The OFF format is used by the princeton shape benchmark (PSB) [2]
 *
 * [1] http://shape.cs.princeton.edu/benchmark/documentation/off_format.html
 *
 * [2] Philip Shilane, Patrick Min, Michael Kazhdan, and Thomas Funkhouser The Princeton Shape Benchmark
 *      Shape Modeling International, Genova, Italy, June 2004
 *      
 * @author rgasser
 * @version 1.1
 */
public class OFFMeshDecoder implements Decoder<Mesh> {
        /** Default logging facility. */
        private static final Logger LOGGER = LogManager.getLogger();

        /** Delimiter used to separate two entries. */
        private static final String DELIMITER = " ";

        /** Token used to denote the beginning of the OFF file. */
        private static final String TOKEN_BOF = "OFF";

        /** HashSet containing all the mime-types supported by this ImageDecoder instance. */
        private static final Set<String> supportedFiles;
        static {
            HashSet<String> tmp = new HashSet<>();
            tmp.add("application/3d-off");
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
         * @param cacheConfig The {@link ImageCacheConfig} used by this {@link Decoder}
         * @return True if initialization was successful, false otherwise.
         */
        @Override
        public boolean init(Path path, DecoderConfig decoderConfig, ImageCacheConfig cacheConfig) {
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
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                /* First line must start with OFF. */
                String line = br.readLine();
                String[] tokens = null;
                if (line == null || !line.startsWith(TOKEN_BOF)) {
                  return null;
                }

                /* Now read second line which should contain the number of vertices and faces. */
                line = br.readLine();
                if (line == null) {
                  return null;
                }
                tokens = line.split(DELIMITER);

                int vertices = Integer.parseInt(tokens[0]);
                int faces = Integer.parseInt(tokens[1]);

                /* Prepare empty mesh. */
                Mesh mesh = new Mesh(faces, vertices);

                /* Now read all the vertices. */
                for (int v=0; v<vertices; v++) {
                    line = br.readLine();
                    if (line == null) {
                        LOGGER.error("Could not decode OFF file {} because file seems to be missing some vertices ({}/{}).", this.inputFile.toString(), v, vertices);
                        return null;
                    }
                    tokens = line.split(DELIMITER);
                    mesh.addVertex(new Vector3f(Float.parseFloat(tokens[0]),Float.parseFloat(tokens[1]),Float.parseFloat(tokens[2])));
                }

                 /* Now read all the faces. */
                for (int f=0; f<faces; f++) {
                    line = br.readLine();
                    if (line == null) {
                        LOGGER.error("Could not decode OFF file {} because file seems to be missing some faces ({}/{}).", this.inputFile.toString(), f, vertices);
                        return null;
                    }
                    tokens = line.split(DELIMITER);
                    if (Integer.parseInt(tokens[0]) == 4) {
                        Vector4i vertexIndex = new Vector4i(Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]),Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4]));
                        if (!mesh.addFace(vertexIndex)) {
                            LOGGER.warn("Could not add face {}/{}/{}/{} because index points to non-existing vertex.",vertexIndex.x, vertexIndex.y, vertexIndex.z, vertexIndex.w);
                        }
                    } else if (Integer.parseInt(tokens[0]) == 3) {
                        Vector3i vertexIndex = new Vector3i(Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]),Integer.parseInt(tokens[3]));
                        if (!mesh.addFace(vertexIndex)) {
                            LOGGER.warn("Could not add face {}/{}/{}/{} because index points to non-existing vertex.",vertexIndex.x, vertexIndex.y, vertexIndex.z);
                        }
                    } else {
                        LOGGER.error("Could not decode OFF file {} because this implementation of Mesh only supports triangular and quadrilateral faces. The provided number of faces is {}.", this.inputFile.toString(), Integer.parseInt(tokens[0]));
                        return null;
                    }
                }

                br.close(); /* Closes the input stream. */

                return mesh;
            } catch (IOException e) {
                LOGGER.error("Could not decode OFF file {} due to an IO exception ({})", this.inputFile.toString(), LogHelper.getStackTrace(e));
                return null;
            } catch (NumberFormatException e) {
                LOGGER.error("Could not decode OFF file {} because one of the tokens could not be converted to a valid number.", this.inputFile.toString(), LogHelper.getStackTrace(e));
                return null;
            } catch (ArrayIndexOutOfBoundsException e) {
                LOGGER.error("Could not decode OFF file {} because one of the faces points to invalid vertex indices.", this.inputFile.toString(), LogHelper.getStackTrace(e));
                return null;
            } finally {
                this.complete.set(true);
            }
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
