package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.VoxelGrid;
import org.vitrivr.cineast.core.data.m3d.Voxelizer;
import org.vitrivr.cineast.core.data.m3d.WritableMesh;
import org.vitrivr.cineast.core.util.mesh.MeshTransformUtil;


public class Model3DSegment implements SegmentContainer {

  /**
   * Default Voxelizer instance used for Mesh voxelization.
   */
  private static final Voxelizer DEFAULT_VOXELIZER = new Voxelizer(0.002f);

  /**
   * Segment ID of the AudioSegment.
   */
  private String segmentId;

  /**
   * ID of the multimedia object this AudioSegment belongs to.
   */
  private String objectId;

  /**
   * The original 3D Mesh as extracted from a model file.
   */
  private final Mesh mesh;

  /**
   * The KHL transformed version of the original Mesh.
   */
  private final Mesh normalizedMesh;

  /**
   * The 3D VoxelGrid associated with the Model3DSegment. This grid is created lazily.
   */
  private final Object gridLock = new Object();
  private VoxelGrid grid;

  /**
   * Default constructor for Model3DSegment
   *
   * @param mesh 3D Mesh associated with the segment.
   */
  public Model3DSegment(Mesh mesh) {
    this.mesh = new Mesh(mesh);
    this.normalizedMesh = MeshTransformUtil.khlTransform(mesh, 1.0f);
  }

  /**
   * @return a unique id of this
   */
  @Override
  public final String getId() {
    return this.segmentId;
  }

  @Override
  public final void setId(String id) {
    this.segmentId = id;
  }

  @Override
  public final String getSuperId() {
    return this.objectId;
  }

  @Override
  public final void setSuperId(String id) {
    this.objectId = id;
  }

  /**
   * Returns a 3D Mesh associated with this Segment.
   *
   * @return Mesh
   */
  @Override
  public final ReadableMesh getMesh() {
    return this.mesh;
  }

  @Override
  public final WritableMesh getNormalizedMesh() {
    return this.normalizedMesh;
  }


  /**
   * Returns the VoxelGrid associated with this Segment. Calculates the grid if needed.
   *
   * @return VoxelGrid
   */
  public final VoxelGrid getGrid() {
    synchronized (this.gridLock) {
      if (this.grid == null) {
        this.grid = DEFAULT_VOXELIZER.voxelize(this.mesh);
      }
    }
    return this.grid;
  }
}
