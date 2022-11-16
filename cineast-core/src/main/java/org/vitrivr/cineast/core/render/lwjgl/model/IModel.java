package org.vitrivr.cineast.core.render.lwjgl.model;

import java.util.List;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.WritableMesh;

public interface IModel {

  List<Entity> getEntities();

  void cleanup();

  String getId();

  List<Material> getMaterials();

}
