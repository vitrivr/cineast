package org.vitrivr.cineast.core.render.lwjgl.engine;


import org.vitrivr.cineast.core.render.lwjgl.glmodel.GLScene;
import org.vitrivr.cineast.core.render.lwjgl.render.Render;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;


/**
 * The EngineLogic provides methods to be called by the engine on certain states.
 */
public abstract class EngineLogic {

  /**
   * Is called from the engine as first step during refresh and cleanup
   * @implSpec DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void cleanup();

  /**
   * Is called once at the initialization of the engine.
   * @implSpec DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void init(Window window, GLScene scene, Render render);

  /**
   * Is called from the engine before the render method.
   * @implSpec DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void beforeRender(Window window, GLScene scene, Render render);

  /**
   * Is called from the engine after the render method.
   * @implSpec DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void afterRender(Window window, GLScene scene, Render render);

  /**
   * This method is called every frame.
   * This is only used in continuous rendering.
   * The purpose is to do some input handling.
   * Could be use for optimize view  angles on a fast manner.
   * @implSpec DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void input(Window window, GLScene scene, long diffTimeMillis);

  /**
   * After Engine run This method is called every frame.
   * This is only used in continuous rendering.
   * The purpose is to process some life output.
   * Could be use for optimize view  angles on a fast manner.
   * @implSpec DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD
   * DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  protected abstract void update(Window window, GLScene scene, long diffTimeMillis);
}
