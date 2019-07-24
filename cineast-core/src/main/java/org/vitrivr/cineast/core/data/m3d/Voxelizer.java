package org.vitrivr.cineast.core.data.m3d;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.util.math.MathConstants;
import org.vitrivr.cineast.core.util.mesh.MeshMathUtil;

/**
 * This class can be used to to transform a 3D polygon Mesh into a 3D VoxelGrid. The class performs this
 * transformation by applying the algorithm described in [1]. It can either use a grid of fixed size or one
 * that dynamically fits the bounding box of the Mesh given the resolution.
 *
 * The resulting VoxelGrid approximates the surface of the Mesh. The volumetric interior of the grid will not
 * be filled!
 *
 * <p>Important:</p> The resolution of the Voxelizer and the size of the Mesh determine the accuracy of the Voxelization
 * process. For instance, if the resolution is chosen such that one Voxel has the size of the whole mesh, the Voxelization
 * will not yield any meaningful result.
 *
 * [1] Huang, J. H. J., Yagel, R. Y. R., Filippov, V. F. V., & Kurzion, Y. K. Y. (1998).
 *  An accurate method for voxelizing polygon meshes. IEEE Symposium on Volume Visualization (Cat. No.989EX300), 119–126,. http://doi.org/10.1109/SVV.1998.729593
 *
 * @author rgasser
 * @version 1.0
 * @created 06.01.17
 */
public class Voxelizer {
    /** */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Resolution, i.e. size of a single voxel. */
    private final float resolution;

    /** Half of the resolution. Pre-calculated for convenience. */
    private final float rc;

    /** Half the resolution squared. Pre-calculated for convenience. */
    private final float rcsq;

    /**
     * Default constructor. Defines the resolution of the Voxelizer.
     *
     * @param resolution Resolution of the Voxelizer (i.e. size of a single Voxel in the grid). This is an important
     * parameter, as it determines the quality of the Voxelization process!
     */
    public Voxelizer(float resolution) {
        this.resolution = resolution;
        this.rc = resolution/2;
        this.rcsq = (float)Math.pow(this.rc,2);
    }

    /**
     * Voxelizes the provided mesh returning a VoxelGrid with the specified resolution. The size of the VoxelGrid
     * will be chose so as to fit the size of the mesh.
     *
     * @param mesh Mesh that should be voxelized.
     * @return VoxelGrid representation of the mesh.
     */
    public VoxelGrid voxelize(ReadableMesh mesh) {
        /* Calculate bounding box of mesh. */
        float[] boundingBox = mesh.bounds();
        short sizeX = (short)(Math.abs(Math.ceil(((boundingBox[0]-boundingBox[1])/this.resolution))) + 1);
        short sizeY = (short)(Math.abs(Math.ceil(((boundingBox[2]-boundingBox[3])/this.resolution))) + 1);
        short sizeZ = (short)(Math.abs(Math.ceil(((boundingBox[4]-boundingBox[5])/this.resolution))) + 1);

        /* Return the voxelized mesh. */
        return this.voxelize(mesh, sizeX, sizeY, sizeZ);
    }

    /**
     * Voxelizes the provided mesh returning a new VoxelGrid with the specified resolution. The size of
     * the VoxelGrid will be fixed
     *
     * @param mesh Mesh that should be voxelized.
     * @param sizeX Number of Voxels in X direction.
     * @param sizeY Number of Voxels in Y direction.
     * @param sizeZ Number of Voxels in Z direction.
     * @return VoxelGrid representation of the mesh.
     */
    public VoxelGrid voxelize(ReadableMesh mesh, int sizeX, int sizeY, int sizeZ) {
         /* Initializes a new voxel-grid. */
        VoxelGrid grid = new VoxelGrid(sizeX, sizeY, sizeZ, this.resolution, false);

        /* Return the voxelized mesh. */
        return this.voxelize(mesh, grid);
    }

    /**
     * Voxelizes the provided mesh into the provided VoxelGrid
     *
     * @param mesh Mesh that should be voxelized.
     * @param grid VoxelGrid to use for voxelization.
     * @return VoxelGrid representation of the mesh.
     */
    public VoxelGrid voxelize(ReadableMesh mesh, VoxelGrid grid) {

        long start =  System.currentTimeMillis();

        /* Process the faces and perform all the relevant tests described in [1]. */
        for (Mesh.Face face : mesh.getFaces()) {
            List<Mesh.Vertex> vertices = face.getVertices();
            List<Pair<Vector3i,Vector3f>> enclosings = Voxelizer.this.enclosingGrid(vertices, grid);
            for (Pair<Vector3i,Vector3f> enclosing : enclosings) {
                /* Perform vertex-tests. */
                if (Voxelizer.this.vertextTest(vertices.get(0), enclosing)) {
                    grid.toggleVoxel(true, enclosing.first.x, enclosing.first.y, enclosing.first.z);
                    continue;
                }
                if (Voxelizer.this.vertextTest(vertices.get(1), enclosing)) {
                    grid.toggleVoxel(true, enclosing.first.x, enclosing.first.y, enclosing.first.z);
                    continue;
                }
                if (Voxelizer.this.vertextTest(vertices.get(2), enclosing)) {
                    grid.toggleVoxel(true, enclosing.first.x, enclosing.first.y, enclosing.first.z);
                    continue;
                }
                 /* Perform edge-tests. */
                if (Voxelizer.this.edgeTest(vertices.get(0), vertices.get(1), enclosing)) {
                    grid.toggleVoxel(true, enclosing.first.x, enclosing.first.y, enclosing.first.z);
                    continue;
                }
                if (Voxelizer.this.edgeTest(vertices.get(1), vertices.get(2), enclosing)) {
                    grid.toggleVoxel(true, enclosing.first.x, enclosing.first.y, enclosing.first.z);
                    continue;
                }
                if (Voxelizer.this.edgeTest(vertices.get(2), vertices.get(0), enclosing)) {
                    grid.toggleVoxel(true, enclosing.first.x, enclosing.first.y, enclosing.first.z);
                    continue;
                }

                /* Perform plane-tests. */
                if (Voxelizer.this.planeTest(vertices.get(0), vertices.get(1), vertices.get(2), enclosing)) {
                    grid.toggleVoxel(true, enclosing.first.x, enclosing.first.y, enclosing.first.z);
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
    private boolean vertextTest(Mesh.Vertex vertex, Pair<Vector3i,Vector3f> voxel) {
       return vertex.getPosition().distanceSquared(voxel.second) > this.rcsq;
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
    private boolean edgeTest(Mesh.Vertex a, Mesh.Vertex b, Pair<Vector3i,Vector3f> voxel) {
        Vector3f line = (new Vector3f(b.getPosition())).sub(a.getPosition());
        Vector3f pd = voxel.second.sub(a.getPosition());

        /* Calculate distance between a and b (Edge). */
        float lsq = a.getPosition().distanceSquared(b.getPosition());
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
     * @param c Third vertex that spans the face.
     * @param voxel Voxel to be tested.
     * @return true if voxel's center is contained in the area, false otherwise.
     */
    private boolean planeTest(Mesh.Vertex a, Mesh.Vertex b, Mesh.Vertex c, Pair<Vector3i,Vector3f> voxel) {
        /* Retrieve center and corner of voxel. */
        Vector3f vcenter = voxel.second;
        Vector3f vcorner = (new Vector3f(this.rc, this.rc, this.rc)).add(vcenter);

        /* Calculate the vectors spanning the plane of the facepolyon and its plane-normal. */
        Vector3f ab = (new Vector3f(b.getPosition())).sub(a.getPosition());
        Vector3f ac = (new Vector3f(c.getPosition())).sub(a.getPosition());
        Vector3f planenorm = (new Vector3f(ab)).cross(ac);

        /* Calculate the distance t for enclosing planes. */
        float t = (float)(this.rc* MathConstants.SQRT3*vcorner.angleCos(vcenter));

        /* Derive new displaced plane normals. */
        Vector3f planenorm_plus = new Vector3f(planenorm.x + t, planenorm.y + t, planenorm.z + t);
        Vector3f planenorm_minus = new Vector3f(planenorm.x - t, planenorm.y - t, planenorm.z - t);

        /* Check if the center is under the planenorm_plus and above the planenorm_minus. */
        return planenorm_plus.dot(vcenter) < 0 && planenorm_minus.dot(vcenter) > 0;
    }

    /**
     * Calculates and returns the enclosing grid, i.e. a list of Voxels from the grid that enclose the list
     * of provided vertices.
     *
     * @param vertices The Vertices for which an enclosing grid needs to be found.
     * @param grid VoxelGrid to select voxels from.
     * @return List of voxels that confine the provided vertices.
     */
    private List<Pair<Vector3i,Vector3f>> enclosingGrid(List<Mesh.Vertex> vertices, VoxelGrid grid) {
        /* Calculate bounding box for provided vertices. */
        ArrayList<Vector3fc> positions = new ArrayList<>(vertices.size());
        for (Mesh.Vertex vertex : vertices) {
            positions.add(vertex.getPosition());
        }

        float bounds[] = MeshMathUtil.bounds(positions);

        /* Derive max and min voxel-indices from bounding-boxes. */
        Vector3i max = grid.coordinateToVoxel(new Vector3f(bounds[0], bounds[2], bounds[4]));
        Vector3i min = grid.coordinateToVoxel(new Vector3f(bounds[1], bounds[3], bounds[5]));

        /* Initialize an empty ArrayList for the Voxel-Elements. */
        List<Pair<Vector3i,Vector3f>> enclosing = new ArrayList<>((max.x-min.x) * (max.y-min.y) * (max.z-min.z));
        for (int i = min.x; i <= max.x; i++) {
            for (int j = min.y;j<= max.y; j++) {
                for (int k = min.z; k <= max.z; k++) {
                    enclosing.add(new Pair<>(new Vector3i(i,j,k), grid.getVoxelCenter(i,j,k)));
                }
            }
        }

        /* Return list of enclosing voxels. */
        return enclosing;
    }
}
