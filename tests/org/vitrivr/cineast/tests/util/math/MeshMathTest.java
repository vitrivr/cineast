package org.vitrivr.cineast.tests.util.math;

import org.joml.Vector3f;

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

    private static final Mesh CUBE = new Mesh(6, 20, 20);
    private static final Mesh DODECAHEDRON = new Mesh(12, 20, 20);
    private static final Mesh ANY = new Mesh(10,10,10);

    static {
        Random random = new Random();
        for (double[] vertex : MathConstants.VERTICES_3D_CUBE) {
            CUBE.addVertex(new Vector3f((float)vertex[0], (float)vertex[1], (float)vertex[2]));
        }

        for (double[] vertex : MathConstants.VERTICES_3D_DODECAHEDRON) {
            DODECAHEDRON.addVertex(new Vector3f((float)vertex[0], (float)vertex[1], (float)vertex[2]));
        }

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
        float[] bounds = MeshMathUtil.bounds(CUBE);
        assertEquals(1.0f, bounds[0],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(-1.0f, bounds[1],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(1.0f, bounds[2],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(-1.0f, bounds[3],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(1.0f, bounds[4],1e-6, "Bounding box of CUBE should be the cube itself!");
        assertEquals(-1.0f, bounds[5],1e-6, "Bounding box of CUBE should be the cube itself!");
    }

    /**
     * Tests the bounding-box of a regular, dodecahedral mesh which should be cube with side-length 2*PHI.
     */
    @Test
    @DisplayName("Dodecahedron Bounding Box Test")
    void testDodecahedronBoundingBox() {
        float[] bounds = MeshMathUtil.bounds(DODECAHEDRON);
        assertEquals(MathConstants.PHI, bounds[0],1e-6, "Bounding box of DODECAHEDRON should range from PHI to -PHI!");
        assertEquals(-MathConstants.PHI, bounds[1],1e-6, "Bounding box of DODECAHEDRON should range from PHI to -PHI!");
        assertEquals(MathConstants.PHI, bounds[2],1e-6, "Bounding box of DODECAHEDRON should range from PHI to -PHI!");
        assertEquals(-MathConstants.PHI, bounds[3],1e-6, "Bounding box of DODECAHEDRON should range from PHI to -PHI!");
        assertEquals(MathConstants.PHI, bounds[4],1e-6, "Bounding box of DODECAHEDRON should range from PHI to -PHI!");
        assertEquals(-MathConstants.PHI, bounds[5],1e-6, "Bounding box of DODECAHEDRON should range from PHI to -PHI!");
    }

    /**
     * Tests the bounding-box of an empty mesh, which should be zero.
     */
    @Test
    @DisplayName("Empty Bounding Box Test")
    void testEmptyBoundingBox() {
        float[] bounds = MeshMathUtil.bounds(Mesh.EMPTY);
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
        assertEquals(0.0f, barycenter.x, 1e-6, "X-Coordinate of CUBE barycenter is off (expected 0.0).");
        assertEquals(0.0f, barycenter.y, 1e-6, "Y-Coordinate of CUBE barycenter is off (expected 0.0).");
        assertEquals(0.0f, barycenter.y, 1e-6, "Z-Coordinate of CUBE barycenter is off (expected 0.0).");
    }

    @Test
    @DisplayName("Dodecahedron Barycenter Test")
    void testDodecahedronBarycenter() {
        Vector3f barycenter = MeshMathUtil.barycenter(DODECAHEDRON);
        assertEquals(0.0f, barycenter.x, 1e-6, "X-Coordinate of DODECAHEDRON barycenter is off (expected 0.0).");
        assertEquals(0.0f, barycenter.y, 1e-6, "Y-Coordinate of DODECAHEDRON barycenter is off (expected 0.0).");
        assertEquals(0.0f, barycenter.y, 1e-6, "Z-Coordinate of DODECAHEDRON barycenter is off (expected 0.0).");
    }

    @Test
    @DisplayName("Empty Barycenter Test")
    void testEmptyBarycenter() {
        Vector3f barycenter = MeshMathUtil.barycenter(Mesh.EMPTY);
        assertEquals(Float.NaN, barycenter.x, "X-Coordinate of EMPTY barycenter is supposed to be NaN.");
        assertEquals(Float.NaN, barycenter.y, "X-Coordinate of EMPTY barycenter is supposed to be NaN.");
        assertEquals(Float.NaN, barycenter.y,"X-Coordinate of EMPTY barycenter is supposed to be NaN.");
    }

    @Test
    @DisplayName("Any Barycenter Test")
    void testAnyBarycenter() {
        Vector3f barycenter = MeshMathUtil.barycenter(ANY);
        assertNotEquals(0.0f, barycenter.x, "X-Coordinate of ANY barycenter is not supposed to be 0.0.");
        assertNotEquals(0.0f, barycenter.y, "Y-Coordinate of ANY barycenter is not supposed to be 0.0.");
        assertNotEquals(0.0f, barycenter.y,"Z-Coordinate of ANY barycenter is not supposed to be 0.0.");
    }
}
