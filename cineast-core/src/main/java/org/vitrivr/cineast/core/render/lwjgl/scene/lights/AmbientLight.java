package org.vitrivr.cineast.core.render.lwjgl.scene.lights;

import org.joml.Vector3f;

public class AmbientLight extends AbstractLight {

  public AmbientLight(Vector3f color, float intensity) {
    super(color, intensity);
  }

  public AmbientLight(LightColor lightColor, float intensity) {
    super(lightColor, intensity);
  }

  public AmbientLight() {
    super(LightColor.WHITE.getUnitRGB(), 0.0f);
  }
}
