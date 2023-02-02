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
import org.vitrivr.cineast.core.data.m3d.texturemodel.util.MinimalBoundingBox;

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
      var mbb = new MinimalBoundingBox();

      for (var material :this.materials) {
        mbb.merge(material.getMinimalBoundingBox());
      }

    entity.setPosition(mbb.getTranslationToNorm().mul(-1));
    entity.setScale(mbb.getScalingFactorToNorm());
    this.entities.add(entity);
    }

  public String getId() {
    return this.id;
  }

  public List<Material> getMaterials() {
    return Collections.unmodifiableList(this.materials);
  }

  /**
   * Returns a list of all normals of all meshes in this model.
   * @return
   */
  public List<Vector3f> getAllNormals() {
    var normals = new ArrayList<Vector3f>();
    this.materials.forEach(m -> m.getMeshes().forEach(mesh -> normals.addAll(mesh.getNormals())));
    return normals;
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
