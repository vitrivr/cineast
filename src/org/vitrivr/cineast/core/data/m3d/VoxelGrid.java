package org.vitrivr.cineast.core.data.m3d;

import com.jogamp.opengl.GL2;
import org.joml.Vector3f;

import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;

/**
 * @author rgasser
 * @version 1.0
 * @created 06.01.17
 */
public class VoxelGrid implements Renderable {
    /**
     * A single Voxel.
     */
    public class Voxel {
        private final short x;
        private final short y;
        private final short z;
        private boolean visible;

        public short getX() {
            return x;
        }
        public short getY() {
            return y;
        }
        public short getZ() {
            return z;
        }
        public Vector3f getCenter() {
            return new Vector3f((x-VoxelGrid.this.sizeX/2) * VoxelGrid.this.resolution, (y-VoxelGrid.this.sizeY/2)*VoxelGrid.this.resolution, (z-VoxelGrid.this.sizeZ/2)*VoxelGrid.this.resolution);
        }
        public boolean getVisible() {
            return visible;
        }

        public Voxel(short x, short y, short z, boolean visible) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.visible = visible;
        }
    }

    /**
     * Determines the size of a single voxel.
     */
    private float resolution;

    /**
     * Determines the half size of a single voxel.
     */
    private float halfResolution;

    /**
     * The size of the voxel grid in X direction.
     */
    private final short sizeX;

    /**
     * The size of the voxel grid in X direction.
     */
    private final short sizeY;

    /**
     * The size of the voxel grid in X direction.
     */
    private final short sizeZ;

    /**
     * The total lenght ot the voxel grid (i.e. the number of voxels in the grid).
     */
    private final int length;

    /**
     * Array holding the actual voxels.
     */
    private final Voxel[][][] voxelGrid;

    /**
     * Defines the center of the voxel-grid (in the world coordinate system)
     */
    private final Vector3f center = new Vector3f(0f,0f,0f);

    /**
     * Handle of the vertex-list.
     */
    private int vertexList = -1;

    /**
     * Default constructor: Initializes a new, fully active Voxel-Grid
     *
     * @param sizeX X-size of the new voxel grid.
     * @param sizeY Y-size of the new voxel grid.
     * @param sizeZ Z-size of the new voxel grid.
     * @param resolution Resolution of the grid, i.e. size of a single voxel.
     */
    public VoxelGrid(short sizeX, short sizeY, short sizeZ, float resolution) {
        this(sizeX, sizeY, sizeZ, resolution, true);
    }

    /**
     * Default constructor: Initializes a new Voxel-Grid.
     *
     * @param sizeX X-size of the new voxel grid.
     * @param sizeY Y-size of the new voxel grid.
     * @param sizeZ Z-size of the new voxel grid.
     * @param resolution Resolution of the grid, i.e. size of a single voxel.
     * @param active Indicates whether the grid should be initialized with active or inactive voxels.
     */
    public VoxelGrid(short sizeX, short sizeY, short sizeZ, float resolution, boolean active) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.resolution = resolution;
        this.halfResolution = resolution/2;
        this.length = sizeX * sizeY * sizeZ;
        this.voxelGrid = new Voxel[sizeX][sizeY][sizeZ];
        for (short i = 0;i<this.sizeX;i++) {
            for (short j = 0;j<this.sizeY;j++) {
                for (short k = 0;k<this.sizeZ;k++) {
                    this.voxelGrid[i][j][k] = new Voxel(i,j,k, active);
                }
            }
        }
    }

    /**
     * Getter for voxel grid's X-size.
     *
     * @return X-size of the voxel grid.
     */
    public final short getSizeX() {
        return sizeX;
    }

    /**
     * Getter for voxel grid's Y-size.
     *
     * @return Y-size of the voxel grid.
     */
    public final short getSizeY() {
        return sizeY;
    }

    /**
     * Getter for voxel grid's Z-size.
     *
     * @return Z-size of the voxel grid.
     */
    public final short getSizeZ() {
        return sizeZ;
    }

    /**
     * Getter for the length of the voxel-grid i.e. the number of voxels
     * in the grid.
     *
     * @return Number of voxels in the grid.
     */
    public final int getLength() {
        return length;
    }

    /**
     *
     * @return
     */
    public final float getResolution() {
        return resolution;
    }

    /**
     *
     * @return
     */
    public final float getHalfResolution() {
        return halfResolution;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public final Voxel get(int x, int y, int z) {
        return this.voxelGrid[x][y][z];
    }

    /**
     *
     * @param visible
     * @param x
     * @param y
     * @param z
     */
    public final void toggle(boolean visible, int x, int y, int z) {
        this.voxelGrid[x][y][z].visible = visible;
    }


    /**
     * Assembles a mesh into a new glDisplayList. The method returns a handle for this
     * newly created glDisplayList. To actually render the list - by executing the commands it contains -
     * the glCallList function must be called!
     *
     * IMPORTANT: The glDisplayList bust be deleted once its not used anymore by calling glDeleteLists

     * @return Handle for the newly created glDisplayList.
     */
    public int assemble(GL2 gl) {
        if (this.vertexList > 0) return this.vertexList;
        this.vertexList = gl.glGenLists(1);
        gl.glNewList(this.vertexList, GL_COMPILE);
        {
            boolean[] visible = {true, true, true, true, true, true};

            for (int i = 0; i < this.sizeX; i++) {
                for (int j = 0; j < this.sizeY; j++) {
                    for (int k = 0; k < this.sizeZ; k++) {
                        /* Skip Voxel if its inactive. */
                        if (!this.voxelGrid[i][j][k].visible) continue;

                        /* Extract center of the voxel. */
                        float x = this.center.x + this.voxelGrid[i][j][k].getCenter().x;
                        float y = this.center.y + this.voxelGrid[i][j][k].getCenter().y;
                        float z = this.center.z + this.voxelGrid[i][j][k].getCenter().z;

                        /* Determine which faces to draw: Faced that are covered by another active voxel are switched off. */
                        if(i > 0) visible[0] = !this.voxelGrid[i-1][j][k].visible;
                        if(i < this.getSizeX()-1) visible[1] = !this.voxelGrid[i+1][j][k].visible;
                        if(j > 0) visible[2] = !this.voxelGrid[i][j-1][k].visible;
                        if(j < this.getSizeY()-1) visible[3] = !this.voxelGrid[i][j+1][k].visible;
                        if(k > 0) visible[4] = !this.voxelGrid[i][j][k-1].visible;
                        if(k < this.getSizeZ()-1) visible[5] = !this.voxelGrid[i][j][k+1].visible;

                        /* Draw the cube. */
                        this.drawCube(gl, x,y,z,visible);
                    }
                }
            }
        }

        gl.glEndList();
        return  this.vertexList;
    }

    /**
     *
     * @param gl
     */
    public void clear(GL2 gl) {
        gl.glDeleteLists(this.vertexList, 1);
        this.vertexList = -1;
    }

    /**
     *
     * @param gl
     * @param x
     * @param y
     * @param z
     * @param visible
     */
    private final void drawCube(GL2 gl, float x, float y, float z, boolean[] visible) {
        gl.glBegin(GL_QUADS);
        {
            /* 1 */
            if (visible[0]) {
                gl.glVertex3f(x + halfResolution, y - halfResolution, z - halfResolution);
                gl.glVertex3f(x - halfResolution, y - halfResolution, z - halfResolution);
                gl.glVertex3f(x - halfResolution, y + halfResolution, z - halfResolution);
                gl.glVertex3f(x + halfResolution, y + halfResolution, z - halfResolution);
            }

            /* 2 */
            if (visible[1]) {
                gl.glVertex3f(x - halfResolution, y - halfResolution, z + halfResolution);
                gl.glVertex3f(x + halfResolution, y - halfResolution, z + halfResolution);
                gl.glVertex3f(x + halfResolution, y + halfResolution, z + halfResolution);
                gl.glVertex3f(x - halfResolution, y + halfResolution, z + halfResolution);
            }

            /* 3 */
            if (visible[2]) {
                gl.glVertex3f(x + halfResolution, y - halfResolution, z + halfResolution);
                gl.glVertex3f(x + halfResolution, y - halfResolution, z - halfResolution);
                gl.glVertex3f(x + halfResolution, y + halfResolution, z - halfResolution);
                gl.glVertex3f(x + halfResolution, y + halfResolution, z + halfResolution);
            }

            /* 4 */
            if (visible[3]) {
                gl.glVertex3f(x - halfResolution, y - halfResolution, z - halfResolution);
                gl.glVertex3f(x - halfResolution, y - halfResolution, z + halfResolution);
                gl.glVertex3f(x - halfResolution, y + halfResolution, z + halfResolution);
                gl.glVertex3f(x - halfResolution, y + halfResolution, z - halfResolution);
            }

            /* 5 */
            if (visible[4]) {
                gl.glVertex3f(x - halfResolution, y - halfResolution, z - halfResolution);
                gl.glVertex3f(x + halfResolution, y - halfResolution, z - halfResolution);
                gl.glVertex3f(x + halfResolution, y - halfResolution, z + halfResolution);
                gl.glVertex3f(x - halfResolution, y - halfResolution, z + halfResolution);
            }

            /* 6 */
            if (visible[5]) {
                gl.glVertex3f(x + halfResolution, y + halfResolution, z - halfResolution);
                gl.glVertex3f(x - halfResolution, y + halfResolution, z - halfResolution);
                gl.glVertex3f(x - halfResolution, y + halfResolution, z + halfResolution);
                gl.glVertex3f(x + halfResolution, y + halfResolution, z + halfResolution);
            }
        }
        gl.glEnd();
    }
}
