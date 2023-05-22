package org.vitrivr.cineast.core.render.lwjgl.scene.lights;

import java.util.Vector;
import org.joml.Vector3f;

public class PointLight extends AbstractLight {

  private Attenuation attenuation;
  private final Vector3f position;

  public PointLight(Vector3f color, Vector3f position , float intensity) {
    super(color, intensity);
    this.attenuation = new Attenuation(0, 0, 1);
    this.position = position;
  }

  public Attenuation getAttenuation() {
    return this.attenuation;
  }

  public Vector3f getPosition() {
    return this.position;
  }

  public void setAttenuation(Attenuation attenuation) {
    this.attenuation = attenuation;
  }

  public void setPosition(Vector3f position) {
    this.position.set(position);
  }
  public void setPosition(float x, float y, float z) {
    this.position.set(x, y, z);
  }

}
