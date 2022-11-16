package org.vitrivr.cineast.core.render;

import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.VoxelGrid;

public interface MeshOnlyRenderer extends Renderer {
  /**
   * Assembles a new Mesh object and thereby adds it to the list of objects that should be rendered.
   *
   * @param mesh Mesh that should be rendered
   */
  void assemble(ReadableMesh mesh);

  /**
   * Assembles a new VoxelGrid object and thereby adds it to the list of objects that should be rendered.
   *
   * @param grid VoxelGrid that should be rendered.
   */
  void assemble(VoxelGrid grid);

}
