package org.vitrivr.cineast.core.data.segments;

import com.jogamp.opengl.Threading.Mode;
import org.jcodec.codecs.common.biari.MDecoder;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.VoxelGrid;
import org.vitrivr.cineast.core.data.m3d.Voxelizer;
import org.vitrivr.cineast.core.data.m3d.WritableMesh;
import org.vitrivr.cineast.core.render.lwjgl.model.IModel;
import org.vitrivr.cineast.core.render.lwjgl.model.Model;
import org.vitrivr.cineast.core.util.mesh.MeshTransformUtil;


public class TextureModel3DSegment implements SegmentContainer {


  /**
   * The original 3D Mesh as extracted from a model file.
   */
  private final Model model;

  /**
   * Segment ID of the ModelSegmen.
   */
  private String segmentId;
  /**
   * ID of the multimedia object this AudioSegment belongs to.
   */
  private String objectId;
  private VoxelGrid grid;

  /**
   * Default constructor for Model3DSegment
   *
   * @param model 3D Mesh associated with the segment.
   */
  public TextureModel3DSegment(Model model) {
    this.model = model;
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

  @Override
  public IModel getModel() {
    return Model.EMPTY;
  }
}
