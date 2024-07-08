package org.vitrivr.cineast.core.render.lwjgl.scene.lights;

import org.joml.Vector3f;

public abstract class AbstractLight {

  private final Vector3f color;
  private float intensity;

  public AbstractLight(Vector3f color, float intensity) {
    this.color = color;
    this.intensity = intensity;
  }
  public AbstractLight(LightColor color, float intensity) {
    this(color.getUnitRGB(), intensity);
  }

  public Vector3f getColor() {
    return this.color;
  }

  public float getIntensity() {
    return this.intensity;
  }
  public void setColor(Vector3f color) {
    this.color.set(color);
  }
  public void setColor(float r, float g, float b) {
    this.color.set(r, g, b);
  }

  public void setIntensity(float intensity) {
    this.intensity = intensity;
  }

}
