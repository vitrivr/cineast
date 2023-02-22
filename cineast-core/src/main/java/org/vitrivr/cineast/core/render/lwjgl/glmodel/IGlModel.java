package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.List;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;

/**
 * The GLModel class is a wrapper for the {@link IModel} class.
 * <ul>
 * <li>IModel -> GLModel( IModel )</li>
 * </ul>
 * <p>
 * The purpose is to bring the generic IModel in an OpenGl context
 * {@link IModel} -> {@link GLModel}
 */
public interface IGlModel {

  /**
   * Returns the entities of the wrapped generic model.
   *
   * @return The entities of the wrapped generic model.
   */
  List<Entity> getEntities();

  /**
   * Adds an entity to the wrapped generic model.
   *
   * @param entity The entity to be added.
   */
  void addEntity(Entity entity);

  /**
   * Cleans up the gl model and calls all underlying cleanup methods.
   * Removes only the references to wrapped generic materials
   * Hence, the model could be used by another extraction task this method does not close the generic model.
   */
  void cleanup();

  /**
   * Returns the id of the wrapped generic model.
   *
   * @return The id of the wrapped generic model.
   */
  String getId();

  /**
   * Returns the gl materials of the gl model.
   *
   * @return The gl materials of the gl model.
   */
  List<GlMaterial> getMaterials();
}
