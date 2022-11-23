package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;

public class GLModel implements IGlModel {

  private IModel model;

  private List<GlMaterial> materials;


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

  public void cleanup() {
    this.materials.stream().forEach(GlMaterial::cleanup);
  }

  public String getId() {
    return this.model.getId();
  }

  public List<GlMaterial> getMaterials() {
    return Collections.unmodifiableList(this.materials);
  }

}
