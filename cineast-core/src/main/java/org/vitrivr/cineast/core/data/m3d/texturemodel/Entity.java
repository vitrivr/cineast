package org.vitrivr.cineast.core.data.m3d.texturemodel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GLScene;

/**
 * An Entity in the context of a {@link Model} describes a position and scale of a model in the scene.
 * The Entity is composed of a model matrix that is used to transform the model in the scene.
 * The model matrix is calculated from the position, rotation and scale of the entity.
 * The Entity influences how the model is rendered in the scene.
 * It does not change the mesh of the model.
 * Neither does it change the viewpoint of the camera.
 */
public class Entity {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * ID of entity.
   */
  private final String id;

  /**
   * ID of associated model.
   */
  private final String modelId;

  /**
   * Model matrix of entity.
   * Used to transform the model in the scene.
   * Calculated from position, rotation and scale.
   */
  private final Matrix4f modelMatrix;

  /**
   * Position of entity.
   */
  private final Vector3f position;

  /**
   * Rotation of entity.
   */
  private final Quaternionf rotation;

  /**
   * Scale of entity.
   */
  private float scale;

  /**
   * Constructs a new Entity.
   * Defines an associated model and an id.
   * With associated model one is able to add new transformations to the Scene {@link GLScene#addEntity(Entity)}.
   *
   * @param id ID of entity.
   * @param modelId ID of associated model.
   */
  public Entity(String id, String modelId) {
    this.id = id;
    this.modelId = modelId;
    this.position = new Vector3f();
    this.rotation = new Quaternionf();
    this.scale = 1f;
    this.modelMatrix = new Matrix4f();
    this.updateModelMatrix();
  }

  /**
   * Returns the id of the entity.
   * @return ID of entity.
   */
  public String getId() {
    return this.id;
  }

  /**
   * Returns the id of the associated model.
   * @return ID of associated model.
   */
  public String getModelId() {
    return this.modelId;
  }

  /**
   * Returns the model matrix of the entity.
   * @return Model matrix of entity.
   */
  public Matrix4f getModelMatrix() {
    return this.modelMatrix;
  }

  /**
   * Returns the position of the entity.
   * @return Position of entity.
   */
  public Vector3f getPosition() {
    return this.position;
  }

  /**
   * Returns the rotation of the entity.
   * @return Rotation of entity.
   */
  public Quaternionf getRotation() {
    return this.rotation;
  }

  /**
   * Returns the scale of the entity.
   * @return Scale of entity.
   */
  public float getScale() {
    return this.scale;
  }

  /**
   * Sets the as a translation vector from the origin.
   * @param x X coordinate of position.
   * @param y Y coordinate of position.
   * @param z Z coordinate of position.
   */
  @SuppressWarnings("unused")
  public void setPosition(float x, float y, float z) {
    this.position.x = x;
    this.position.y = y;
    this.position.z = z;
  }

  /**
   * Sets the as a translation vector from the origin.
   * @param position Position of entity.
   */
  public void setPosition(Vector3f position) {
    this.position.set(position);
  }

  /**
   * Sets the rotation of the entity.
   * @param x X coordinate of axis.
   * @param y Y coordinate of axis.
   * @param z Z coordinate of axis.
   * @param angle Angle of rotation.
   */
  public void setRotation(float x, float y, float z, float angle) {
    this.rotation.fromAxisAngleRad(x, y, z, angle);
  }

  /**
   * Sets the rotation of the entity.
   * @param axis Axis of rotation.
   * @param angle Angle of rotation.
   */
  public void setRotation(Vector3f axis, float angle) {
    this.rotation.fromAxisAngleRad(axis, angle);
  }

  /**
   * Sets the scale of the entity.
   * set to 1 for no scaling.
   * @param scale Scale of entity.
   */
  public void setScale(float scale) {
    this.scale = scale;
  }

  /**
   * Updates the model matrix of the entity.
   * This has to be called after any transformation.
   */
  public void updateModelMatrix() {
    this.modelMatrix.translationRotateScale(this.position, this.rotation, this.scale);
  }

  /**
   * Closes the entity.
   * Sets the position, rotation and scale to zero.
   */
  public void close() {
    this.position.zero();
    this.rotation.identity();
    this.scale = 1;
    this.updateModelMatrix();
    LOGGER.trace("Entity {} closed", this.id);
  }
}
