package org.vitrivr.cineast.core.util.mesh;

import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.Mesh;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of utilities surrounding Mesh mathematics. Includes methods to calculate the barycenter or the
 * bounding box of a Mesh.
 *
 * [1] Vranić, D. and D. S. (n.d.). 3D Model Retrieval.
 *
 * @author rgasser
 * @version 1.0
 * @created 08.01.17
 */
public final class MeshMathUtil {


   /**
     * Private constructor; cannot be instantiated.
     */
    private MeshMathUtil() {}

    /**
     * Returns the vertex from a mesh that is farthest away from a given point.
     *
     * @param mesh Mesh from which the farthest vertex should be selected.
     * @param point Point to which the distance should be calculated.
     * @return Coordinates of the vertex that is farthest to the provided point.
     */
    public static Vector3f farthestVertex(Mesh mesh, Vector3f point) {
        List<Vector3f> vertices = mesh.getVertices();
        Vector3f max = vertices.get(0);
        float dsq_max = point.distanceSquared(max);
        for (Vector3f v : vertices) {
            float dsq = point.distanceSquared(v);
            if (dsq > dsq_max) {
                dsq_max = dsq;
                max = v;
            }
        }
        return max;
    }

    /**
     * Returns the vertex from a mesh that is closest to a given point.
     *
     * @param mesh Mesh from which the closest vertex should be selected.
     * @param point Point to which the distance should be calculated.
     * @return Coordinates of the vertex that is closest to the provided point.
     */
    public static Vector3f closestVertex(Mesh mesh, Vector3f point) {
        List<Vector3f> vertices = mesh.getVertices();
        Vector3f min = vertices.get(0);
        float dsq_min = point.distanceSquared(min);
        for (Vector3f v : mesh.getVertices()) {
            float dsq = point.distanceSquared(v);
            if (dsq < dsq_min) {
                dsq_min = dsq;
                min = v;
            }
        }
        return min;
    }

    /**
     * Calculates the center of mass (barycenter) of a polyhedral mesh by obtaining
     * the mean of the Mesh's face centroids weighted by the area of the respective face as
     * described in [1].
     *
     * @param mesh The mesh for which the barycenter should be calculated.
     * @return Coordinates of the barycenter.
     */
    public static Vector3f barycenter(Mesh mesh) {
        Vector3f barycenter = new Vector3f(0f,0f,0f);
        double total = 0.0;
        for (Mesh.Face face : mesh.getFaces()) {
            double area = face.area();
            total += area;
            barycenter.add(face.centroid().mul((float)area));
        }
        barycenter.div((float)total);
        return barycenter;
    }


    /**
     * Calculates and returns the bounds for the provided mesh.
     *
     * @param mesh Mesh for which bounds should be calculated.
     * @return Float-array spanning the bounds: {max_x, min_x, max_y, min_y, max_z, min_z}
     */
    public static float[] bounds(Mesh mesh) {
        /* Extract all vertices that are part of a face. */
        List<Vector3f> vertices = new ArrayList<>(mesh.numberOfVertices());
        for (Mesh.Face face : mesh.getFaces()) {
            for (Vector3f vertex : face.getVertices()) {
                vertices.add(vertex);
            }
        }

       return bounds(vertices);
    }

    /**
     * Calculates and returns the bounds for the provided mesh.
     *
     * @param vertices Vertices for which bounds should be calculated.
     * @return Float-array spanning the bounds: {max_x, min_x, max_y, min_y, max_z, min_z}
     */
    public static float[] bounds(List<Vector3f> vertices) {
        /* If no vertices are in the list, the box is zero. */
        if (vertices.isEmpty()) {
            return new float[6];
        }

        /* Initialize the bounding-box. */
        float bounds[] = {
                -Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, Float.MAX_VALUE
        };

        /* Find max and min y-values. */
        for(Vector3f vertex : vertices) {
            if (vertex.x() > bounds[0]) bounds[0] = vertex.x();
            if (vertex.x() < bounds[1]) bounds[1] = vertex.x();
            if (vertex.y() > bounds[2]) bounds[2] = vertex.y();
            if (vertex.y() < bounds[3]) bounds[3] = vertex.y();
            if (vertex.z() > bounds[4]) bounds[4] = vertex.z();
            if (vertex.z() < bounds[5]) bounds[5] = vertex.z();
        }

        /* Return bounding-box. */
        return bounds;
    }
}
