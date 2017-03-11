package org.vitrivr.cineast.core.data.m3d;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.joml.Vector3f;
import org.vitrivr.cineast.core.util.mesh.MeshMathUtil;

import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 06.01.17
 */
public class Voxelizer {
    /** */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Constant: Square-root of 3. */
    private static final float sqrt3 =  (float)Math.sqrt(3);

    /** Resolution, i.e. size of a single voxel. */
    private final float resolution;

    /** Half of the resolution. Pre-calculated for convenience. */
    private final float rc;

    /** Half the resolution squared. Pre-calculated for convenience. */
    private final float rcsq;

    /**
     * Default constructor. Defines the resolution of the Voxelizer.
     *
     * @param resolution
     */
    public Voxelizer(float resolution) {
        this.resolution = resolution;
        this.rc = resolution/2;
        this.rcsq = (float)Math.pow(this.rc,2);
    }

    /**
     * Voxelizes the provided mesh, returning a VoxelGrid with the specified resolution.
     *
     * @param mesh Mesh that should be voxelized.
     * @return Voxel representation of the mesh.
     */
    public VoxelGrid voxelize(Mesh mesh) {
        long start =  System.currentTimeMillis();

        /* Calculate bounding box of mesh. */
        float[] boundingBox = MeshMathUtil.bounds(mesh);
        short sizeX = (short)(Math.abs(Math.ceil(((boundingBox[0]-boundingBox[1])/this.resolution))) + 1);
        short sizeY = (short)(Math.abs(Math.ceil(((boundingBox[2]-boundingBox[3])/this.resolution))) + 1);
        short sizeZ = (short)(Math.abs(Math.ceil(((boundingBox[4]-boundingBox[5])/this.resolution))) + 1);

        /* Initializes a new voxel-grid. */
        VoxelGrid grid = new VoxelGrid(sizeX, sizeY, sizeZ, this.resolution, false);

        /* Prepare emtpy HashSets. */
        for (Mesh.Face face : mesh.getFaces()) {
            List<Vector3f> vertices = face.getVertices();
            List<VoxelGrid.Voxel> enclosings = Voxelizer.this.enclosingGrid(vertices, boundingBox, grid);
            for (VoxelGrid.Voxel enclosing : enclosings) {
                /* Perform vertex-tests. */
                if (Voxelizer.this.vertextTest(vertices.get(0), enclosing)) {
                    grid.toggle(true, enclosing.getX(), enclosing.getY(), enclosing.getZ());
                    continue;
                }
                if (Voxelizer.this.vertextTest(vertices.get(1), enclosing)) {
                    grid.toggle(true, enclosing.getX(), enclosing.getY(), enclosing.getZ());
                    continue;
                }
                if (Voxelizer.this.vertextTest(vertices.get(2), enclosing)) {
                    grid.toggle(true, enclosing.getX(), enclosing.getY(), enclosing.getZ());
                    continue;
                }
                 /* Perform edge-tests. */
                if (Voxelizer.this.edgeTest(vertices.get(0), vertices.get(1), enclosing)) {
                    grid.toggle(true, enclosing.getX(), enclosing.getY(), enclosing.getZ());
                    continue;
                }
                if (Voxelizer.this.edgeTest(vertices.get(1), vertices.get(2), enclosing)) {
                    grid.toggle(true, enclosing.getX(), enclosing.getY(), enclosing.getZ());
                    continue;
                }
                if (Voxelizer.this.edgeTest(vertices.get(2), vertices.get(0), enclosing)) {
                    grid.toggle(true, enclosing.getX(), enclosing.getY(), enclosing.getZ());
                    continue;
                }

                /* Perform plane-tests. */
                if (Voxelizer.this.planeTest(vertices.get(0), vertices.get(1), vertices.get(2), enclosing)) {
                    grid.toggle(true, enclosing.getX(), enclosing.getY(), enclosing.getZ());
                }
            }
        }

        long stop = System.currentTimeMillis();
        LOGGER.log(Level.DEBUG, String.format("Voxelization of mesh completed in %d ms (Size: %d x %d x %d).", (stop - start), grid.getSizeX(), grid.getSizeY(), grid.getSizeZ()));

        return grid;
    }

    /**
     * Performs the vertex-test described in [1]. Checks if the provided voxel's center is within the area
     * of circle with radius L/2 around the vertex (L being the size of a voxel).
     *
     * @param vertex Vertex to be tested.
     * @param voxel Voxel to be tested.
     * @return true if the voxel's center is within the circle, false otherwise.
     */
    private boolean vertextTest(Vector3f vertex, VoxelGrid.Voxel voxel) {
       return vertex.distanceSquared(voxel.getCenter()) > this.rcsq;
    }

    /**
     * Performs the edge-test described in [1]. Checks if the provided voxel's center is enclosed in the cylinder
     * around the line between vertex a and vertex b.
     *
     * @param a First vertex that constitutes the line used to draw a cylinder around.
     * @param b Second vertex that constitutes the line used to draw a cylinder around.
     * @param voxel Voxel to be tested.
     * @return true if voxel's center is contained in cylinder, false otherwise.
     */
    private boolean edgeTest(Vector3f a, Vector3f b, VoxelGrid.Voxel voxel) {
        Vector3f line = (new Vector3f(b)).sub(a);
        Vector3f pd = voxel.getCenter().sub(a);

        /* Calculate distance between a and b (Edge). */
        float lsq = a.distanceSquared(b);
        float dot = line.dot(pd);

        if (dot < 0.0f || dot > lsq) {
            return false;
        } else {
            float dsq = pd.lengthSquared() - ((float)Math.pow(dot,2))/lsq;
            return dsq > rc;
        }
    }

    /**
     * Performs a simplified version of the plane-test described in [1]. Checks if the provided voxel's center is
     * enclosed in the space spanned by two planes parallel to the face.
     *
     * The original version of the test performs three addition checks with planes that go through the edges. These
     * tests are ommited because we only work on a reduced set of voxels that directly enclose the vertices in question.
     *
     * @param a First vertex that spans the face.
     * @param b Second vertex that spans the face.
     * @param c Thrid vertex that spans the face.
     * @param voxel Voxel to be tested.
     * @return true if voxel's center is contained in the area, false otherwise.

     */
    private boolean planeTest(Vector3f a, Vector3f b, Vector3f c, VoxelGrid.Voxel voxel) {
        /* Retrieve center and corner of voxel. */
        Vector3f vcenter = voxel.getCenter();
        Vector3f vcorner = (new Vector3f(this.rc, this.rc, this.rc)).add(vcenter);

        /* Calculate the vectors spanning the plane of the facepolyon and its plane-normal. */
        Vector3f ab = (new Vector3f(b)).sub(a);
        Vector3f ac = (new Vector3f(c)).sub(a);
        Vector3f planenorm = (new Vector3f(ab)).cross(ac);

        /* Calculate the distance t for enclosing planes. */
        float t = this.rc*sqrt3*vcorner.angleCos(voxel.getCenter());

        /* Derive new displaced plane normals. */
        Vector3f planenorm_plus = new Vector3f(planenorm.x + t, planenorm.y + t, planenorm.z + t);
        Vector3f planenorm_minus = new Vector3f(planenorm.x - t, planenorm.y - t, planenorm.z - t);

        /* Check if the center is under the planenorm_plus and above the planenorm_minus. */
        return planenorm_plus.dot(vcenter) < 0 && planenorm_minus.dot(vcenter) > 0;
    }

    /**
     * Calculates and returns the enclosing grid, i.e. a list of voxels from the
     * grid that enclose the list of provided vertices.
     *
     * @param vertices The Vertices for which an enclosing grid needs to be found.
     * @param boundingBox Bounding-box of the mesh. Used for calculations.
     * @param grid VoxelGrid to select voxels from.
     * @return List of voxels that confine the provided vertices.
     */
    private List<VoxelGrid.Voxel> enclosingGrid(List<Vector3f> vertices, float[] boundingBox, VoxelGrid grid) {
        /* Calculate bounding box for provided vertices. */
        float bounds[] = MeshMathUtil.bounds(vertices);

        /* Derive max and min voxel-indices from bounding-boxes. */
        int max_x = Math.abs((int)Math.ceil(((boundingBox[1]-bounds[0])/this.resolution)));
        int min_x = Math.abs((int)Math.ceil(((boundingBox[1]-bounds[1])/this.resolution)));
        int max_y = Math.abs((int)Math.ceil(((boundingBox[3]-bounds[2])/this.resolution)));
        int min_y = Math.abs((int)Math.ceil(((boundingBox[3]-bounds[3])/this.resolution)));
        int max_z = Math.abs((int)Math.ceil(((boundingBox[5]-bounds[4])/this.resolution)));
        int min_z = Math.abs((int)Math.ceil(((boundingBox[5]-bounds[5])/this.resolution)));

        assert min_x >= 0;
        assert max_x < grid.getSizeX();
        assert min_x <= max_x;

        assert min_y >= 0;
        assert max_y < grid.getSizeY();
        assert min_y <= max_y;

        assert min_z >= 0;
        assert max_z < grid.getSizeY();
        assert min_z <= max_z;

        /* Initialize an empty ArrayList for the Voxel-Elements. */
        List<VoxelGrid.Voxel> enclosing = new ArrayList<>((max_x-min_x) * (max_y-min_y) * (max_z-min_z));
        for (int i = min_x; i <= max_x; i++) {
            for (int j = min_y;j<= max_y; j++) {
                for (int k = min_z; k <= max_z; k++) {
                    enclosing.add(grid.get(i,j,k));
                }
            }
        }

        /* Return list of enclosing voxels. */
        return enclosing;
    }
}
