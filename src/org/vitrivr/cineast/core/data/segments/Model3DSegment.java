package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.VoxelGrid;
import org.vitrivr.cineast.core.data.m3d.Voxelizer;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.03.17
 */
public class Model3DSegment implements SegmentContainer {

    /** Default Voxelizer instance used for Mesh voxelization. */
    private static final Voxelizer DEFAULT_VOXELIZER = new Voxelizer(0.002f);

    /** Segment ID of the AudioSegment. */
    private String segmentId;

    /** ID of the multimedia object this AudioSegment belongs to. */
    private String objectId;

    /** The 3D Mesh associated with the Model3DSegment. */
    private final Mesh mesh;

    /** The 3D VoxelGrid associated with the Model3DSegment. This grid is created lazily. */
    private AtomicReference<VoxelGrid> grid = new AtomicReference<>();

    /**
     * Default constructor for Model3DSegment
     *
     * @param mesh 3D Mesh associated with the segment.
     */
    public Model3DSegment(Mesh mesh) {
        this.mesh = mesh;
    }

    /**
     * @return a unique id of this
     */
    @Override
    public final String getId() {
        return this.segmentId;
    }

    /**
     * @param id
     * @return a unique id of this
     */
    @Override
    public final void setId(String id) {
        this.segmentId = id;
    }

    /**
     *
     * @return
     */
    @Override
    public final String getSuperId() {
        return this.objectId;
    }

    /**
     * @param id
     */
    @Override
    public final void setSuperId(String id) {
        this.objectId = id;
    }

    /**
     * Returns a 3D Mesh associated with this Segment.
     *
     * @return Mesh
     */
    public final Mesh getMesh() {
        return this.mesh;
    }

    /**
     * Returns the VoxelGrid associated with this Segment. Calculates the
     * grid if needed.
     *
     * @return VoxelGrid
     */
    public final VoxelGrid getGrid() {
        if (this.grid.get() == null) {
            this.grid.set(DEFAULT_VOXELIZER.voxelize(this.mesh));
        }
        return this.grid.get();
    }
}
