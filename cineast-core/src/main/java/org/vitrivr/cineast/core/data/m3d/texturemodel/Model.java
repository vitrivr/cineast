package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Model implements IModel {

  private final String id;

  private List<Entity> entities;
  private List<Material> materials;

  public static final Model EMPTY = new Model("EmptyModel", new ArrayList<>() {{add(Material.EMPTY);}});


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
  public void addEntityNorm(Entity entity) {
      var min = Float.MAX_VALUE;
      for (var mesh:this.materials) {
        min = Math.min(min,mesh.getMaxNormalizedScalingFactor());
      }
    entity.setScale(min);
    this.entities.add(entity);
  }

  public String getId() {
    return this.id;
  }

  public List<Material> getMaterials() {
    return Collections.unmodifiableList(this.materials);
  }

}
