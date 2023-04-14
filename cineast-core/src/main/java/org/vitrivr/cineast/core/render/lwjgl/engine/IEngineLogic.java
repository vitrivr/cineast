package org.vitrivr.cineast.core.render.lwjgl.engine;


import org.vitrivr.cineast.core.render.lwjgl.glmodel.GLScene;
import org.vitrivr.cineast.core.render.lwjgl.render.Render;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;


/**
 * The EngineLogic provides methods to be called by the engine on certain states.
 */
public abstract class IEngineLogic {

  /**
   * Is called from the engine as first step during refresh and cleanup
   * DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void cleanup();

  /**
   * Is called once at the initialization of the engine.
   * DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void init(Window window, GLScene scene, Render render);

  /**
   * Is called from the engine before the render method.
   * DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void beforeRender(Window window, GLScene scene, Render render);

  /**
   * Is called from the engine after the render method.
   * DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void afterRender(Window window, GLScene scene, Render render);

  /**
   * This method is called every frame.
   * This is only used in continuous rendering.
   * The purpose is to do some input handling.
   * Could be use for optimize view  angles on a fast manner.
   * DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void input(Window window, GLScene scene, long diffTimeMillis);

  /**
   * After Engine run This method is called every frame.
   * This is only used in continuous rendering.
   * The purpose is to process some life output.
   * Could be use for optimize view  angles on a fast manner.
   * DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void update(Window window, GLScene scene, long diffTimeMillis);
}
