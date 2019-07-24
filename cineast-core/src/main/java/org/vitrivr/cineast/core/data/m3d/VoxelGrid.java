package org.vitrivr.cineast.core.data.m3d;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

/**
 * This class represents a Voxel grid, i.e. a 3-dimensional grid of 3D pixels (called Voxels). Every voxel
 * can either be visible or invisible.
 *
 * @author rgasser
 * @version 1.0
 * @created 06.01.17
 */
public class VoxelGrid {
    /** The default, empty VoxelGrid. */
    public static final VoxelGrid EMPTY = new VoxelGrid(5,5,5,0.1f);

    /** Represents a single Voxel which can either can be visible or invisible. */
    public enum Voxel {
        VISIBLE, INVISIBLE
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

    /** Number of visible voxels in the grid. */
    private int visible = 0;

    /** Number of invisible voxels in the grid. */
    private int invisible = 0;

    /** Array holding the actual voxels. */
    private final Voxel[][][] voxelGrid;

    /** Defines the center of the voxel-grid (in the world coordinate system). It corresponds to the
     * center of the voxel at (sizeX/2, sizeY/2, sizeZ/2).
     *
     *
     * <p>Important: </p> Transformation into world coordinates are based on this center!
     */
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
     * @param active Indicates whether the grid should be initialized with active or inactive Voxels.
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
                    if (active) {
                        this.voxelGrid[i][j][k] = Voxel.VISIBLE;
                        this.visible += 1;
                    } else {
                        this.voxelGrid[i][j][k] = Voxel.INVISIBLE;
                        this.invisible += 1;
                    }
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
     * @return Number of Voxels in the grid.
     */
    public final int getLength() {
        return length;
    }

    /**
     * Getter for the center of the voxel-grid.
     *
     * @return Vector pointing to the center of the grid.
     */
    public final Vector3fc getGridCenter() {
        return center;
    }

    /**
     * Getter for the number of visible Voxels in the grid.
     *
     * @return Number of visible Voxels in the grid.
     */
    public final int getVisible() {
        return visible;
    }

    /**
     * Getter for the number of invisible Voxels in the grid.
     *
     * @return Number of invisible Voxels in the grid.
     */
    public final int getInvisible() {
        return invisible;
    }

    /**
     * Returns true if VoxelGrid is visible (i.e. there is at least one
     * visible Voxel) and false otherwise.
     *
     * @return
     */
    public final boolean isVisible() {
        return this.visible > 0;
    }

    /**
     * Returns the resolution of the Voxel grid, i.e. the size of a single
     * Voxel.
     *
     * @return Size of a single voxel.
     */
    public final float getResolution() {
        return resolution;
    }

    /**
     * Transforms world-coordinates (e.g. position of a vertex in space) into the corresponding voxel coordinates,
     * i.e. the index of the voxel in the grid.
     *
     * <p>Important: </p> The indices returned by this method are not necessarily within the bounds
     * of the grid.
     *
     * @param coordinate Coordinates to be transformed.
     * @return coordinate Voxel indices.
     */
    public final Vector3i coordinateToVoxel(Vector3fc coordinate) {
        Vector3f gridCoordinates = (new Vector3f(coordinate)).add(this.center).div(this.resolution);
        return new Vector3i((int)Math.ceil(gridCoordinates.x + this.sizeX/2), (int)Math.ceil(gridCoordinates.y + this.sizeY/2), (int)Math.ceil(gridCoordinates.z + this.sizeZ/2));
    }

    /**
     * Returns the Voxel at the specified position.
     *
     * @param x x position of the Voxel.
     * @param y y position of the Voxel.
     * @param z z position of the Voxel.
     * @throws ArrayIndexOutOfBoundsException If one of the three indices is larger than the grid.
     */
    public final Voxel get(int x, int y, int z) {
        return this.voxelGrid[x][y][z];
    }

    /**
     * Returns true, if the Voxel at the specified position is visible and false otherwise.
     *
     * @param x x position of the Voxel.
     * @param y y position of the Voxel.
     * @param z z position of the Voxel.
     * @throws ArrayIndexOutOfBoundsException If one of the three indices is larger than the grid.
     */
    public final boolean isVisible(int x, int y, int z) {
        return this.voxelGrid[x][y][z] == Voxel.VISIBLE;
    }

    /**
     * Calculates and returns the center of the Voxel in a 3D coordinate system using the grids
     * resolution property.
     *
     * @param x x position of the Voxel.
     * @param y y position of the Voxel.
     * @param z z position of the Voxel.
     * @return Vector3f containing the center of the voxel.
     * @throws ArrayIndexOutOfBoundsException If one of the three indices is larger than the grid.
     */
    public Vector3f getVoxelCenter(int x, int y, int z) {
        return new Vector3f((x-VoxelGrid.this.sizeX/2) * VoxelGrid.this.resolution + this.center.x, (y-VoxelGrid.this.sizeY/2)*VoxelGrid.this.resolution + this.center.y, (z-VoxelGrid.this.sizeZ/2)*VoxelGrid.this.resolution + this.center.z);
    }

    /**
     * Toggles the Voxel at the specified position.
     *
     * @param visible If true, the new Voxel position will become visible.
     * @param x x position of the Voxel.
     * @param y y position of the Voxel.
     * @param z z position of the Voxel.
     * @throws ArrayIndexOutOfBoundsException If one of the three indices is larger than the grid.
     */
    public final void toggleVoxel(boolean visible, int x, int y, int z) {
        if (visible && this.voxelGrid[x][y][z] == Voxel.INVISIBLE) {
            this.voxelGrid[x][y][z] = Voxel.VISIBLE;
            this.invisible -= 1;
            this.visible += 1;
        } else if (!visible && this.voxelGrid[x][y][z] == Voxel.VISIBLE) {
            this.voxelGrid[x][y][z] = Voxel.INVISIBLE;
            this.invisible += 1;
            this.visible -= 1;
        }
    }

    /**
     * Converts the VoxelGrid into a string that can be read by Matlab (e.g. for 3D scatter plots).
     * The array contains the coordinates of all visible voxels.
     *
     * @return String
     */
    public String toMatlabArray() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int x=0;x<this.sizeX;x++) {
            for (int y=0;y<this.sizeY;y++) {
                for (int z=0;z<this.sizeZ;z++) {
                    if (this.voxelGrid[x][y][z] == Voxel.VISIBLE) {
                        buffer.append(String.format("%d %d %d; ",x,y,z));
                    }
                }
            }
        }
        buffer.append("]");
        return buffer.toString();
    }
}
