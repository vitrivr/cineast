package org.vitrivr.cineast.core.render.lwjgl.scene.lights;

import java.util.Vector;
import java.util.function.Supplier;
import org.joml.Vector3f;

public class PointLight extends AbstractLight {

  private Attenuation attenuation;
  private Supplier<Vector3f> position;

  public PointLight(Vector3f color, Vector3f position , float intensity) {
    this(color, () -> position, intensity);
  }

  public PointLight(Vector3f color, Supplier<Vector3f> position , float intensity) {
    super(color, intensity);
    this.attenuation = new Attenuation(0, 0, 1);
    this.position = position;
  }

  public Attenuation getAttenuation() {
    return this.attenuation;
  }

  public Vector3f getPosition() {
    return this.position.get();
  }

  public void setAttenuation(Attenuation attenuation) {
    this.attenuation = attenuation;
  }

  public void setPosition(Supplier<Vector3f> position) {
    this.position = position;
  }

  public void setPosition(Vector3f position) {
    this.position = () -> position;
  }

  public void setPosition(float x, float y, float z) {
    this.position = () -> new Vector3f(x, y, z);
  }

}
