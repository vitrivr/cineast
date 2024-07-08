package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class GLModel implements IGLModel {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * The model that is wrapped by this gl model.
   */
  private final IModel model;

  /**
   * The contained materials in gl context
   */
  private final List<GLMaterial> materials;

  /**
   * Creates a new GLModel from a model.
   *
   * @param model The model that is wrapped by this gl model.
   */
  public GLModel(IModel model) {
    this.model = model;
    this.materials = new ArrayList<>();
    this.model.getMaterials().forEach(material -> this.materials.add(new GLMaterial(material)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Entity> getEntities() {
    return Collections.unmodifiableList(this.model.getEntities());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEntity(Entity entity) {
    this.model.addEntity(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cleanup() {
    this.materials.forEach(GLMaterial::cleanup);
    this.materials.clear();
    LOGGER.debug("GLModel cleaned up");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId() {
    return this.model.getId();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GLMaterial> getMaterials() {
    return Collections.unmodifiableList(this.materials);
  }

  @Override
  public boolean usesNonDefaultTexture() {
    return this.model.usesNonDefaultTexture();
  }
}
