package org.vitrivr.cineast.core.render.lwjgl.engine;


import org.vitrivr.cineast.core.render.lwjgl.render.Render;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GlScene;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;


public interface IEngineLogic {

  void cleanup();

  void init(Window window, GlScene scene, Render render);

  void beforeRender(Window window, GlScene scene, Render render);
  void afterRender(Window window, GlScene scene, Render render);

  void input(Window window, GlScene scene, long diffTimeMillis);

  void update(Window window, GlScene scene, long diffTimeMillis);
}
