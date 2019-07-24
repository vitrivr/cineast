package org.vitrivr.cineast.core.data.m3d;

import org.joml.Vector3fc;

import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 15.03.17
 */
public interface ReadableMesh {
    /**
     * Returns the list of vertices. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of vertices.
     */
    List<Mesh.Vertex> getVertices();

    /**
     * Accessor for an individual vertex.
     *
     * @param vertexIndex Index of the vertex that should be returned.
     * @return Vertex.
     */
    public Mesh.Vertex getVertex(int vertexIndex);

    /**
     * Returns the list of vertex-normals. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of faces.
     */
    List<Mesh.Face> getFaces();

    /**
     * Returns the number of vertices in this Mesh.
     *
     * @return Number of vertices.
     */
    int numberOfVertices();

    /**
     * Returns the number of faces in this Mesh.
     *
     * @return Number of faces.
     */
    int numberOfFaces();

    /**
     * Returns the total surface area of the Mesh.
     *
     * @return Surface area of the mesh.
     */
    double surfaceArea();

    /**
     * Calculates and returns the Mesh's bounding-box
     *
     * @return Bounding-box of the mesh.
     */
    float[] bounds();

    /**
     * Calculates and returns the Mesh's barycenter.
     *
     * @return Barycenter of the Mesh.
     */
    Vector3fc barycenter();

    /**
     * Indicates, whether the mesh is an empty Mesh or not
     *
     * @return True if mesh is empty, false otherwise.
     */
    boolean isEmpty();
}
