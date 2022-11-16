package org.vitrivr.cineast.core.render.lwjgl.engine;


import org.vitrivr.cineast.core.render.lwjgl.render.Render;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;

public interface IEngineLogic {

  void cleanup();

  void init(Window window, Scene scene, Render render);

  void input(Window window, Scene scene, long diffTimeMillis);

  void update(Window window, Scene scene, long diffTimeMillis);
}
