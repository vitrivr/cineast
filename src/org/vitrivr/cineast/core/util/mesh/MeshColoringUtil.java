package org.vitrivr.cineast.core.util.mesh;

import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.Mesh;

import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 08.01.17
 */
public final class MeshColoringUtil {
    /**
     * Private constructor; cannot be instantiated.
     */
    private MeshColoringUtil() {}

    /**
     * Adds colours to the vertices proportional to their distance to the barycenter.
     *
     * @param mesh Mesh that needs coloring.
     */
    public static void color(Mesh mesh) {
        List<Vector3f> vertices = mesh.getVertices();
        Vector3f center = MeshMathUtil.barycenter(mesh);
        Vector3f farthestVertex = MeshMathUtil.farthestVertex(mesh, center);
        float ds_max = center.distance(farthestVertex);
        int i = 0;
        for (Vector3f v : vertices) {
            float ds = center.distance(v);
            float colour =  ds/ds_max;
            mesh.updateColor(i, new Vector3f(colour, colour, colour));
            i++;
        }
    }
}
