package org.vitrivr.cineast.core.data.m3d.texturemodel;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Entity {

  private final String id;
  private final String modelId;
  private final Matrix4f modelMatrix;
  private final Vector3f position;
  private final Quaternionf rotation;
  private float scale;

  public Entity(String id, String modelId) {
    this.id = id;
    this.modelId = modelId;
    this.position = new Vector3f();
    this.rotation = new Quaternionf();
    this.scale = 1;
    this.modelMatrix = new Matrix4f();
    this.updateModelMatrix();
  }

  public String getId() {
    return this.id;
  }

  public String getModelId() {
    return this.modelId;
  }

  public Matrix4f getModelMatrix() {
    return this.modelMatrix;
  }

  public Vector3f getPosition() {
    return this.position;
  }

  public Quaternionf getRotation() {
    return this.rotation;
  }

  public float getScale() {
    return this.scale;
  }


  public void setPosition(float x, float y, float z) {
    this.position.x = x;
    this.position.y = y;
    this.position.z = z;
  }
  public void setPosition(Vector3f position) {
    this.position.set(position);
  }

  public void setRotation(float x, float y, float z, float angle) {
    this.rotation.fromAxisAngleRad(x, y, z, angle);
  }

  public void setRotation(Vector3f axis, float angle) {
    this.rotation.fromAxisAngleRad(axis, angle);
  }

  public void setScale(float scale) {
    this.scale = scale;
  }
  public void updateModelMatrix() {
    this.modelMatrix.translationRotateScale(this.position, this.rotation, this.scale);
  }
}
