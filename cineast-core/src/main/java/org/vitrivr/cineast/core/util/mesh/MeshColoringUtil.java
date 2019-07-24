package org.vitrivr.cineast.core.util.mesh;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.WritableMesh;

import java.awt.*;

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
    public static void color(WritableMesh mesh) {
        Vector3fc center = mesh.barycenter();
        Mesh.Vertex farthestVertex = MeshMathUtil.farthestVertex(mesh, center);
        float ds_max = center.distance(farthestVertex.getPosition());
        for (int i=0; i<mesh.numberOfVertices(); i++) {
            float gray = center.distance(mesh.getVertex(i).getPosition())/ds_max;
            Color color =  new Color(gray,gray,gray);
            mesh.updateColor(i, color);
        }
    }

    /**
     *
     * @param mesh
     */
    public static void normalColoring(WritableMesh mesh) {
        Vector3f axis = new Vector3f(1.0f,0.0f, 0.0f);

        Vector3fc center = mesh.barycenter();
        Mesh.Vertex farthestVertex = MeshMathUtil.farthestVertex(mesh, center);
        float ds_max = center.distance(farthestVertex.getPosition());

        for (int i=0; i<mesh.numberOfVertices(); i++) {
            float hue = (float)(axis.angle(mesh.getVertex(i).getNormal())/Math.PI);
            float saturation = center.distance(mesh.getVertex(i).getPosition())/ds_max;

            Color color = Color.getHSBColor(hue, saturation, 1.0f);
            mesh.updateColor(i, color);
        }
    }
}
