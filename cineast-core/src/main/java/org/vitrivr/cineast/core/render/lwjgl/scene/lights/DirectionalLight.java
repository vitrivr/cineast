package org.vitrivr.cineast.core.render.lwjgl.scene.lights;

import java.util.Vector;
import java.util.function.Supplier;
import org.joml.Vector3f;

public class DirectionalLight extends AbstractLight {

  private Supplier<Vector3f> direction;

  public DirectionalLight() {
    super(LightColor.WHITE, 0.0f);
    this.direction = () -> new Vector3f(0.0f, 1f, 0.0f);
  }

  public DirectionalLight(LightColor color, Vector3f direction, float intensity) {
    super(color, intensity);
    this.direction = () -> direction;
  }

  public DirectionalLight(LightColor color, Supplier<Vector3f> direction, float intensity) {
    super(color, intensity);
    this.direction = direction;
  }

  public Vector3f getDirection() {
    return this.direction.get();
  }

  public void setDirection(Vector3f direction) {
    this.direction = () -> direction;
  }
}
