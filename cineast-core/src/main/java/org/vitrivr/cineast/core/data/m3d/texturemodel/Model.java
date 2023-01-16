package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.security.auth.login.AccountLockedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Model implements IModel {
  private static final Logger LOGGER = LogManager.getLogger();

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
      var minScale = Float.MAX_VALUE;
      var maxPosition = new Vector3f(0, 0, 0);
      for (var mesh:this.materials) {
        minScale = Math.min(minScale,mesh.getMaxNormalizedScalingFactor());
        maxPosition = maxPosition.length() > mesh.getMaxNormalizedPosition().length() ? maxPosition : mesh.getMaxNormalizedPosition();
      }
    entity.setPosition(maxPosition.mul(-1));
    entity.setScale(minScale);
    this.entities.add(entity);
    }

  public String getId() {
    return this.id;
  }

  public List<Material> getMaterials() {
    return Collections.unmodifiableList(this.materials);
  }

  public void replaceTextureWithColor(Vector4f color){
    for (var material:this.materials) {
      material.setTexture(new Texture());
      material.setDiffuseColor(color);
    }
  }

  public void close(){
    this.materials.stream().forEach(Material::close);
    this.materials.clear();
    this.materials = null;
    this.entities.stream().forEach(Entity::close);
    this.entities.clear();
    this.entities = null;
    LOGGER.trace("Closed model {}", this.id);
  }
}
