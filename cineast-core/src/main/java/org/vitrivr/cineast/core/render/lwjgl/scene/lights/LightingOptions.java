package org.vitrivr.cineast.core.render.lwjgl.scene.lights;

import org.joml.Vector3f;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.IGLModel;
import org.vitrivr.cineast.core.render.lwjgl.scene.Camera;
import org.vitrivr.cineast.core.render.lwjgl.scene.ILocateable;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;

public enum LightingOptions {
  STATIC,
  LIGHTING_ON_NO_TEXTURE;



  public boolean hasNonDefaultTexture = true;

  public SceneLights getSceneLigths(Scene scene) {
    switch (this) {
      case STATIC -> {
        return new SceneLights();
      }
      case LIGHTING_ON_NO_TEXTURE -> {
        if (this.hasNonDefaultTexture) {
          return new SceneLights();
        } else {
          return new SceneLights(scene.getCamera()::getPosition);
        }
      }
      default -> {
        return new SceneLights();
      }
    }
  }

}
