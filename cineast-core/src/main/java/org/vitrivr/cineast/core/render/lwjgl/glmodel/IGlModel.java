package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.List;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;

public interface IGlModel {

  List<Entity> getEntities();
  void addEntity(Entity entity);


  void cleanup();

  String getId();

  List<GlMaterial> getMaterials();
}
