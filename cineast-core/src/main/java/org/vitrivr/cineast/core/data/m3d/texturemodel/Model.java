package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Model implements IModel {

  private final String id;

  private List<Entity> entities;
  private List<Material> materials;

  public 


  public Model(String id, List<Material> materials) {
    this.id = id;
    this.entities = new ArrayList<>();
    this.materials = materials;
  }

  public List<Entity> getEntities() {
    return Collections.unmodifiableList(this.entities);
  }


  public void addEntity(Entity entity) {
    this.entities.add(entity);
  }

  public String getId() {
    return this.id;
  }

  public List<Material> getMaterials() {
    return Collections.unmodifiableList(this.materials);
  }

}
