package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;

public class GLModel implements IGlModel {
  private static final Logger LOGGER = LogManager.getLogger();
  private final IModel model;

  private final List<GlMaterial> materials;


  public GLModel(IModel model) {
    this.model = model;
    this.materials = new ArrayList<>();
    this.model.getMaterials().stream().forEach(material -> this.materials.add(new GlMaterial(material)));
  }

  public List<Entity> getEntities() {
    return Collections.unmodifiableList(this.model.getEntities());
  }

  public void addEntity(Entity entity) {
    this.model.addEntity(entity);
  }

  /**
   * Cleans the GLModel
   * Does not affect the underlying model
   */
  public void cleanup() {
    this.materials.stream().forEach(GlMaterial::cleanup);
    this.materials.clear();
    LOGGER.debug("GLModel cleaned up");
  }

  public String getId() {
    return this.model.getId();
  }

  public List<GlMaterial> getMaterials() {
    return Collections.unmodifiableList(this.materials);
  }

}
