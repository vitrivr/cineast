package org.vitrivr.cineast.standalone.tests.util.math;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.util.math.MathConstants;
import org.vitrivr.cineast.core.util.mesh.MeshMathUtil;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author rgasser
 * @version 1.0
 * @created 16.03.17
 */
public class MeshMathTest {

    private static final Mesh CUBE = new Mesh(6, 20);
    private static final Mesh ANY = new Mesh(10,10);

    static {
        Random random = new Random();
        for (double[] vertex : MathConstants.VERTICES_3D_CUBE) {
            CUBE.addVertex(new Vector3f((float)vertex[0], (float)vertex[1], (float)vertex[2]));
        }

        CUBE.addFace(new Vector4i(1, 4, 5, 3));
        CUBE.addFace(new Vector4i(4, 7, 0, 5));
        CUBE.addFace(new Vector4i(5, 0, 6, 3));
        CUBE.addFace(new Vector4i(1, 2, 6, 3));
        CUBE.addFace(new Vector4i(1, 4, 7, 2));
        CUBE.addFace(new Vector4i(7, 0, 6, 2));

        for (int i=0;i<100;i++) {
            ANY.addVertex(new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()));
        }
    }

    /**
     * Tests the bounding-box of a cube mesh which should be the cube itself.
     */
    @Test
    @DisplayName("Cube Bounding Box Test")
    void testCubeBoundingBox() {
        float[] bounds = CUBE.bounds();
        assertEquals(1.0f, bounds[0],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(-1.0f, bounds[1],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(1.0f, bounds[2],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(-1.0f, bounds[3],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(1.0f, bounds[4],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(-1.0f, bounds[5],1e-6, "Bounding box of CUBE should be the cube itself!");
    }

    /**
     * Tests the bounding-box of an empty mesh, which should be zero.
     */
    @Test
    @DisplayName("Empty Bounding Box Test")
    void testEmptyBoundingBox() {
        float[] bounds = Mesh.EMPTY.bounds();
        assertEquals(0.0f, bounds[0],1e-6, "Bounding box of EMPTY should be zero.");
        assertEquals(0.0f, bounds[1],1e-6, "Bounding box of EMPTY should be zero.");
        assertEquals(0.0f, bounds[2],1e-6, "Bounding box of EMPTY should be zero.");
        assertEquals(0.0f, bounds[3],1e-6, "Bounding box of EMPTY should be zero.");
        assertEquals(0.0f, bounds[4],1e-6, "Bounding box of EMPTY should be zero.");
        assertEquals(0.0f, bounds[5],1e-6, "Bounding box of EMPTY should be zero.");
    }

    @Test
    @DisplayName("Cube Barycenter Test")
    void testCubeBaryCenter() {
        Vector3f barycenter = MeshMathUtil.barycenter(CUBE);
        assertEquals(0.0f, barycenter.x, 1e-6, "X-Coordinate of CUBE barycenter is off.");
        assertEquals(0.0f, barycenter.y, 1e-6, "Y-Coordinate of CUBE barycenter is off.");
        assertEquals(0.0f, barycenter.z, 1e-6, "Z-Coordinate of CUBE barycenter is off.");
    }

    @Test
    @DisplayName("Cube Barycenter Test")
    void testMovedCubeBaryCenter() {
        Mesh movedCube = new Mesh(CUBE);
        movedCube.move(new Vector3f(1.0f, 1.0f,1.0f));
        Vector3fc barycenter = movedCube.barycenter();
        assertEquals(1.0f, barycenter.x(), 1e-6, "X-Coordinate of CUBE barycenter is off.");
        assertEquals(1.0f, barycenter.y(), 1e-6, "Y-Coordinate of CUBE barycenter is off.");
        assertEquals(1.0f, barycenter.z(), 1e-6, "Z-Coordinate of CUBE barycenter is off.");
    }

    @Test
    @DisplayName("Empty Barycenter Test")
    void testEmptyBarycenter() {
        Vector3fc barycenter = MeshMathUtil.barycenter(Mesh.EMPTY);
        assertEquals(Float.NaN, barycenter.x(), "X-Coordinate of EMPTY barycenter is supposed to be NaN.");
        assertEquals(Float.NaN, barycenter.y(), "X-Coordinate of EMPTY barycenter is supposed to be NaN.");
        assertEquals(Float.NaN, barycenter.z(),"X-Coordinate of EMPTY barycenter is supposed to be NaN.");
    }

    @Test
    @DisplayName("Any Barycenter Test")
    void testAnyBarycenter() {
        Vector3fc barycenter = MeshMathUtil.barycenter(ANY);
        assertNotEquals(0.0f, barycenter.x(), "X-Coordinate of ANY barycenter is not supposed to be 0.0.");
        assertNotEquals(0.0f, barycenter.y(), "Y-Coordinate of ANY barycenter is not supposed to be 0.0.");
        assertNotEquals(0.0f, barycenter.z(),"Z-Coordinate of ANY barycenter is not supposed to be 0.0.");
    }
}
