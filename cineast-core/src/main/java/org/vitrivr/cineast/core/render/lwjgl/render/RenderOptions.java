
package org.vitrivr.cineast.core.render.lwjgl.render;

import java.util.function.Function;
import org.joml.Vector4f;
import org.vitrivr.cineast.core.render.lwjgl.scene.lights.LightingOptions;

/**
 * RenderOptions
 * <ul>
 * <li>Used to switch on or off the texture rendering</li>
 * <li>Used to switch on or off the coloring rendering</li>
 * <li>Returns the color for the given value</li>
 * <li>Can be used to colorize the model custom</li>
 * </ul>
 */
public class RenderOptions {

  /**
   * Used to switch on or off the texture rendering
   */
  public boolean showTextures = true;

  /**
   * Used to switch on or off the coloring rendering For future face coloring
   */
  @SuppressWarnings("unused")
  public boolean showColor = false;


  public LightingOptions lightingOptions = LightingOptions.LIGHTING_ON_NO_TEXTURE;

  /**
   * Returns the color for the given value Can be used to colorize the model custom
   */
  public Function<Float, Vector4f> colorfunction =
      (v) -> new Vector4f(v, v, v, 1f);

}
