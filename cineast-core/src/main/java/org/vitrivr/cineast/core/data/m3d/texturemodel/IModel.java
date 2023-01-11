package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.List;
import org.joml.Vector4f;

public interface IModel {

  List<Entity> getEntities();

  void addEntity(Entity entity);

  String getId();

  List<Material> getMaterials();
  void replaceTextureWithColor(Vector4f color);

}
