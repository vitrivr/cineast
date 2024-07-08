package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.List;
import org.joml.Vector3f;
import org.joml.Vector4f;

public interface IModel {

  /**
   * Returns a list of all entities that are associated with this model.
   * @return List of {@link Entity} objects.
   */
  List<Entity> getEntities();

  /**
   * Adds an entity to the model.
   * @param entity Entity to be added.
   */
  void addEntity(Entity entity);

  /**
   * Returns the id of the model.
   * @return ID of the model.
   */
  String getId();

  /**
   * Returns a list of all materials that are associated with this model.
   * @return List of {@link Material} objects.
   */
  List<Material> getMaterials();

  /**
   * Returns a list of all vertices that are associated with this model.
   * @return List of {@link Vector3f} objects.
   */
  List<Vector3f> getAllNormals();

  boolean usesNonDefaultTexture();

}
