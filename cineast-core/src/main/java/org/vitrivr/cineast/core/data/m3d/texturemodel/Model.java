package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.util.MinimalBoundingBox;

/**
 * This class represents a model that can be rendered by the {@link org.vitrivr.cineast.core.render.lwjgl.engine.Engine}.
 * The model is composed of a list of
 * {@link Entity} objects
 * and a list of {@link Material} objects.
 * The  {@link Entity} objects are used to position and scale the model in the scene.
 * The {@link Material} objects are used to define the appearance of the model.
 */
public class Model implements IModel {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * ID of the model.
   */
  private final String id;

  /**
   * List of {@link Entity} objects that define the position and scale of the model.
   */
  private final List<Entity> entities;

  /**
   * List of {@link Material} objects that define the appearance of the model.
   * Contains all Meshes and Textures that are used by the model.
   */
  private final List<Material> materials;


  /**
   * Empty model that can be used as a placeholder.
   */
  public static final Model EMPTY = new Model("EmptyModel", new ArrayList<>() {{add(Material.EMPTY);}});


  /**
   * Constructor for Model.
   *
   * @param id ID of the model.
   * @param materials List of {@link Material} objects that define the appearance of the model.
   */
  public Model(String id, List<Material> materials) {
    this.id = id;
    this.entities = new ArrayList<>();
    this.materials = materials;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Entity> getEntities() {
    return Collections.unmodifiableList(this.entities);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEntity(Entity entity) {
    this.entities.add(entity);
  }

  /**
   * Adds an entity to the model and normalizes the model.
   * @param entity Entity to be added.
   */
  public void addEntityNorm(Entity entity) {
      var mbb = new MinimalBoundingBox();

      for (var material :this.materials) {
        mbb.merge(material.getMinimalBoundingBox());
      }

    entity.setPosition(mbb.getTranslationToNorm().mul(-1));
    entity.setScale(mbb.getScalingFactorToNorm());
    this.entities.add(entity);
    }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId() {
    return this.id;
  }

  /**
   * {@inheritDoc}
   */
   @Override
  public List<Material> getMaterials() {
    return Collections.unmodifiableList(this.materials);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Vector3f> getAllNormals() {
    var normals = new ArrayList<Vector3f>();
    this.materials.forEach(m -> m.getMeshes().forEach(mesh -> normals.addAll(mesh.getNormals())));
    return normals;
  }

  /**
   * Replaces the texture of all materials with a solid color.
   * @param color Color to be used.
   */
  @Override
  public void replaceTextureWithColor(Vector4f color){
    for (var material:this.materials) {
      material.setTexture(new Texture());
      material.setDiffuseColor(color);
    }
  }

  /**
   * Closes the model and releases all resources.
   */
  public void close(){
    this.materials.forEach(Material::close);
    this.materials.clear();
    this.entities.forEach(Entity::close);
    this.entities.clear();
    LOGGER.trace("Closed model {}", this.id);
  }
}
