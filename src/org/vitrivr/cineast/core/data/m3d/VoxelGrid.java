package org.vitrivr.cineast.core.data.m3d;

import org.joml.Vector3f;

/**
 * @author rgasser
 * @version 1.0
 * @created 06.01.17
 */
public class VoxelGrid {

    /** The default, empty VoxelGrid. */
    public static final VoxelGrid EMPTY = new VoxelGrid(5,5,5,0.1f);

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

    /** Determines the size of a single voxel. */
    private float resolution;

    /** The size of the voxel grid in X direction. */
    private final int sizeX;

    /** The size of the voxel grid in X direction. */
    private final int sizeY;

    /** The size of the voxel grid in X direction. */
    private final int sizeZ;

    /** The total length ot the voxel grid (i.e. the number of voxels in the grid). */
    private final int length;

    /** Array holding the actual voxels. */
    private final Voxel[][][] voxelGrid;

    /** Defines the center of the voxel-grid (in the world coordinate system). */
    private final Vector3f center = new Vector3f(0f,0f,0f);

    /**
     * Default constructor: Initializes a new, fully active Voxel-Grid
     *
     * @param sizeX X-size of the new voxel grid.
     * @param sizeY Y-size of the new voxel grid.
     * @param sizeZ Z-size of the new voxel grid.
     * @param resolution Resolution of the grid, i.e. size of a single voxel.
     */
    public VoxelGrid(int sizeX, int sizeY, int sizeZ, float resolution) {
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
    public VoxelGrid(int sizeX, int sizeY, int sizeZ, float resolution, boolean active) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.resolution = resolution;
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
    public final int getSizeX() {
        return sizeX;
    }

    /**
     * Getter for voxel grid's Y-size.
     *
     * @return Y-size of the voxel grid.
     */
    public final int getSizeY() {
        return sizeY;
    }

    /**
     * Getter for voxel grid's Z-size.
     *
     * @return Z-size of the voxel grid.
     */
    public final int getSizeZ() {
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
     * Getter for the center of the voxel-grid.
     *
     * @return Vector pointing to the center of the grid.
     */
    public Vector3f getCenter() {
        return center;
    }

    /**
     * Returns the resolution of the Voxel, i.e. the size of a single
     * Voxel.
     *
     * @return
     */
    public final float getResolution() {
        return resolution;
    }

    /**
     * Returns half the resolution of the Voxel, i.e. the size of
     * half a Voxel.
     *
     * @return
     */
    public final float getHalfResolution() {
        return this.resolution/2.0f;
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
}
