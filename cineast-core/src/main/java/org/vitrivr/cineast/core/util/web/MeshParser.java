package org.vitrivr.cineast.core.util.web;

import java.io.IOException;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.util.LogHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.03.17
 */
public class MeshParser extends DataURLParser {

    /** Some format specific constants. */
    private static final String MIME_TYPE = "application/3d-json";
    private static final String VERTICES_PROPERTY_NAME_THREEV4 = "vertices";
    private static final String NORMAL_PROPERTY_NAME_THREEV4 = "normals";
    private static final String ARRAY_PROPERTY_NAME_THREEV4 = "array";

    /**
     * Parses a Base64 encoded data url and treats it as Geometry JSON used by the Three.js JavaScript library.
     * Tries to parse the structure into a 3D mesh.
     *
     * @param dataUrl Data URL that should be parsed.
     * @return Mesh, if parsing fails that Mesh will be empty!
     */
    public static Mesh parseThreeJSV4Geometry(String dataUrl) {
		/* Convert Base64 string into byte array. */
        byte[] bytes = dataURLtoByteArray(dataUrl, MIME_TYPE);

        ObjectMapper mapper = new ObjectMapper();
        try {
            /* Read the JSON structure of the transmitted mesh data. */
            JsonNode node = mapper.readTree(bytes);
            JsonNode vertices = node.get(VERTICES_PROPERTY_NAME_THREEV4);
            if (vertices == null || !vertices.isArray() || vertices.size() == 0)  {
                LOGGER.error("Submitted mesh does not contain any vertices. Aborting...");
                return Mesh.EMPTY;
            }

            /* Create new Mesh. */
            Mesh mesh = new Mesh(vertices.size()/9, vertices.size()/3);

            /* Prepare helper structures. */
            TObjectIntHashMap<Vector3f> vertexBuffer = new TObjectIntHashMap<>();
            int index = 0;
            int[] vertexindices = new int[3];

            /* Add all the vertices and normals in the structure. */
            for (int i=0; i<=vertices.size()-9; i+=9) {
                for (int j=0; j<3; j++) {
                    int idx = i + 3*j;
                    Vector3f vertex = new Vector3f((float)vertices.get(idx).asDouble(), (float)vertices.get(idx+1).asDouble(),(float)vertices.get(idx+2).asDouble());
                    if (!vertexBuffer.containsKey(vertex)) {
                        vertexBuffer.put(vertex, index++);
                        mesh.addVertex(vertex);
                    }
                    vertexindices[j] = vertexBuffer.get(vertex);
                }

                mesh.addFace(new Vector3i(vertexindices[0], vertexindices[1], vertexindices[2]));
            }

            return mesh;
        } catch (IOException e) {
            LOGGER.error("Could not create 3d mesh from Base64 input because the file-format is not supported. {}", LogHelper.getStackTrace(e));
            return Mesh.EMPTY;
        }
    }

    /**
     * Checks, if provided data URL is a valid Three v4 JSON geometry. Returns true if so and
     * false otherwise. No structural analysis is performed! Only the raw, data URL is
     * being checked.
     *
     * @param dataUrl Data URL that should be checked.
     * @return True, if data URL is a valid Three v4 JSON geometry.
     */
    public static boolean isValidThreeJSV4Geometry(String dataUrl) {
        return isValidDataUrl(dataUrl, MIME_TYPE);
    }
}
