package org.vitrivr.cineast.core.render.lwjgl.engine;


import org.vitrivr.cineast.core.render.lwjgl.render.Render;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GlScene;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;


/**
 *
 */
public abstract class IEngineLogic {

  protected abstract void cleanup();

  protected abstract void init(Window window, GlScene scene, Render render);

  protected abstract void beforeRender(Window window, GlScene scene, Render render);

  protected abstract void afterRender(Window window, GlScene scene, Render render);

  protected abstract void input(Window window, GlScene scene, long diffTimeMillis);

  protected abstract void update(Window window, GlScene scene, long diffTimeMillis);
}
