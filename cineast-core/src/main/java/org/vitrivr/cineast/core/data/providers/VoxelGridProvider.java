package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.m3d.VoxelGrid;


public interface VoxelGridProvider {
    /**
     * Returns a 3D VoxelGrid. Defaults to the empty VoxelGrid,
     * if not implemented.
     *
     * @return VoxelGrid
     */
    default VoxelGrid getVoxelgrid() {
        return VoxelGrid.EMPTY;
    }
}
