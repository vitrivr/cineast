package org.vitrivr.cineast.core.data.m3d;

import java.awt.Color;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4i;

/**
 * @author rgasser
 * @version 1.0
 * @created 15.03.17
 */
public interface WritableMesh extends ReadableMesh {
    /**
     * Adds a new triangular face to the Mesh. Faces index vertices and
     * vertex normals.
     *
     * @param vertices Vector3i containing the indices of the vertices.
     * @return true if face could be added and false otherwise (e.g. if indices point to non-existing vertex)
     */
    boolean addFace(Vector3i vertices);

    /**
     * Adds a new quadratic face to the Mesh. Faces index vertices and
     * vertex normals.
     *
     * @param vertices Vector4i containing the indices of the vertices.
     * @return true if face could be added and false otherwise (e.g. if indices point to non-existing vertex)
     */
    boolean addFace(Vector4i vertices);

    /**
     * Scales the Mesh by the provided factor. This will reset the surfaceArea and bounding-box property.
     *
     * @param factor Factor by which the Mesh should be scaled. Values < 1.0 will cause the Mesh to shrink.
     */
    void scale(float factor);

    /**
     * Moves the Mesh in the direction of the provided vector.
     *
     * @param translation Vector describing the translation in the three directions.
     */
    void move(Vector3f translation);

    /**
     * Applies a transformation matrix on the Mesh by applying it to all its vertices.
     *
     * @param transformation Transformation matrix that should be applied.
     */
   void transform(Matrix4f transformation);

    /**
     * Updates the color of a vertex.
     *
     * @param vertexIndex Index of the vertex that should be upadated.
     * @param color New color of the vertex.
     */
    void updateColor(int vertexIndex, Color color);
}
