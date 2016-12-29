package org.vitrivr.cineast.core.data.m3d;

import org.joml.Vector3i;
import org.joml.Vector4i;

/**
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class Face {

    private FaceType type;
    private Vector4i normalIndices;
    private Vector4i vertexIndices;

    public Face(Vector3i vertices, Vector3i normals) {
        this.type = FaceType.TRI;
        this.vertexIndices = new Vector4i(vertices, 0);
        if (normals != null) this.normalIndices = new Vector4i(normals, 0);
    }

    public Face(Vector4i normals, Vector4i vertices) {
        this.type = FaceType.TRI;
        this.normalIndices = normals;
        this.vertexIndices = vertices;
    }

    public Vector4i getNormalIndices() {
        return normalIndices;
    }

    public Vector4i getVertexIndices() {
        return vertexIndices;
    }

    public FaceType getType() {
        return type;
    }
}
