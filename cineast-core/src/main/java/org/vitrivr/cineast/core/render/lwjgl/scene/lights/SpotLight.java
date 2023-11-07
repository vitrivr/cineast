package org.vitrivr.cineast.core.render.lwjgl.scene.lights;

import org.joml.Vector3f;

public class SpotLight extends PointLight{
  private final Vector3f coneDirection;
  private float cutOff;
  private float cutOffAngle;

  public SpotLight(Vector3f color, Vector3f position, float intensity, Vector3f coneDirection, float cutOffAngle) {
    super(color, position, intensity);
    this.coneDirection = coneDirection;
    this.cutOffAngle = cutOffAngle;

  }
  public SpotLight(PointLight pointLight, Vector3f coneDirection, float cutOffAngle) {
    this(pointLight.getColor(), pointLight.getPosition(), pointLight.getIntensity(), coneDirection, cutOffAngle);
  }

  public Vector3f getConeDirection() {
    return this.coneDirection;
  }
  public float getCutOff() {
    return this.cutOff;
  }
  public float getCutOffAngle() {
    return this.cutOffAngle;
  }

  public PointLight getPointLight() {
    return (PointLight) this;
  }
  public void setConeDirection(Vector3f coneDirection) {
    this.coneDirection.set(coneDirection);
  }
  public void setConeDirection(float x, float y, float z) {
    this.coneDirection.set(x, y, z);
  }

  public void setCutOffAngle(float cutOffAngle) {
    this.cutOffAngle = cutOffAngle;
  }

  public void setPointLight(PointLight pointLight) {
    this.setColor(pointLight.getColor());
    this.setPosition(pointLight.getPosition());
    this.setIntensity(pointLight.getIntensity());
  }
}
