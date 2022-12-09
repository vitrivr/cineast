
package org.vitrivr.cineast.core.render.lwjgl.render;

import java.util.Vector;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.logging.log4j.core.appender.rewrite.MapRewritePolicy.Mode;
import org.joml.Vector4f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Model;

public class RenderOptions {

  public boolean showTextures = true;
  public boolean showColor = false;
  public Function<Float, Vector4f> colorfunction =
      (v) -> new Vector4f(v, v, v, 1f);

}
