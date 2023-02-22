
package org.vitrivr.cineast.core.render.lwjgl.render;

import java.util.function.Function;
import org.joml.Vector4f;

public class RenderOptions {

  /**
   * Used to switch on or off the texture rendering
   */
  public boolean showTextures = true;

  /**
   * Used to switch on or off the coloring rendering
   */
  public boolean showColor = false;

  /**
   * Returns the color for the given value
   * Can be used to colorize the model customly
   */
  public Function<Float, Vector4f> colorfunction =
      (v) -> new Vector4f(v, v, v, 1f);

}
