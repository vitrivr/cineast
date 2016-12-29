package org.vitrivr.cineast.core.decode.m3d;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4i;
import org.vitrivr.cineast.core.data.m3d.Face;
import org.vitrivr.cineast.core.data.m3d.Mesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class OBJMeshDecoder implements MeshDecoder {

    private Path inputFile;

    /**
     *
     * @param inputFile
     */
    public OBJMeshDecoder(Path inputFile) {
        this.inputFile = inputFile;
    }


    @Override
    public Mesh getMesh() throws MeshDecoderException {

        try {
            InputStream is = Files.newInputStream(this.inputFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            Mesh mesh = new Mesh();
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "v":
                        mesh.addVertex(new Vector3f(Float.parseFloat(tokens[1]),Float.parseFloat(tokens[2]),Float.parseFloat(tokens[3])));
                        break;
                    case "vn":
                        mesh.addNormal(new Vector3f(Float.parseFloat(tokens[1]),Float.parseFloat(tokens[2]),Float.parseFloat(tokens[3])));
                        break;
                    case "f":
                        boolean quad = (tokens.length == 5);
                        String[] p1 = tokens[1].split("/");
                        String[] p2 = tokens[2].split("/");
                        String[] p3 = tokens[3].split("/");

                        if (quad) {
                            String[] p4 = tokens[4].split("/");

                            Vector4i vertexIndex = new Vector4i(Integer.parseInt(p1[0]),Integer.parseInt(p2[0]),Integer.parseInt(p3[0]),Integer.parseInt(p4[0]));
                            Vector4i normalIndex = null;
                            if (p1.length == 3 && p2.length == 3 && p3.length == 3  && p4.length == 3) {
                                normalIndex = new Vector4i(Integer.parseInt(p1[2]),Integer.parseInt(p2[2]),Integer.parseInt(p3[2]),Integer.parseInt(p4[2]));
                            }
                        } else {
                            /* Prepare Vertex-Index and Normal-Index vectors for Tri face. */
                            Vector3i vertexIndex = new Vector3i(Integer.parseInt(p1[0]),Integer.parseInt(p2[0]),Integer.parseInt(p3[0]));
                            Vector3i normalIndex = null;
                            if (p1.length == 3 && p2.length == 3 && p3.length == 3) {
                                normalIndex = new Vector3i(Integer.parseInt(p1[2]),Integer.parseInt(p2[2]),Integer.parseInt(p3[2]));
                            }

                            /* Create and add face. */
                            Face face = new Face(vertexIndex, normalIndex);
                            mesh.addFace(face);
                        }
                        break;
                    default:
                        break;
                }
            }

            return mesh;
        } catch (IOException e) {
            e.printStackTrace();
            throw new MeshDecoderException("An error occurred while loading the mesh.", e);
        }
    }
}
