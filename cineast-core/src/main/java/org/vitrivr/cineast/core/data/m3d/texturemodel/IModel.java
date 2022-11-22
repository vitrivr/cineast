package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.List;

public interface IModel {

  List<Entity> getEntities();

  void addEntity(Entity entity);

  String getId();

  List<Material> getMaterials();

}
