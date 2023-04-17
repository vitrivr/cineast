package org.vitrivr.cineast.core.render.lwjgl.engine;

import org.vitrivr.cineast.core.render.lwjgl.render.Render;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
import org.vitrivr.cineast.core.render.lwjgl.scene.Camera;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GLScene;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;

/**
 * The engine is the main class of the rendering engine.
 * It holds the window, the scene and the render object.
 * It provides a render loop for continuous rendering and  a runOnce method to render a single frame rendering.
 */
public class Engine {
  /**
   * The window object.
   */
  private final Window window;
  /**
   * Indicates whether the engine is running in continuous rendering mode.
   */
  private boolean running;
  /**
   * The render object.
   */
  private final Render render;
  /**
   * The scene object.
   */
  private final GLScene scene;
  /**
   * The application logic. Connects the engine to the overlaying application.
   * The Engine calls the methods of the appLogic object depending on the engine state.
   */
  private final EngineLogic appLogic;
  /**
   * The target frames per second.
   */
  private final int targetFps;
  /**
   * The target updates per second. (e.g inputs rotation,...)
   */
  private final int targetUps;

  /**
   * Creates a new engine
   * @param windowTitle The title of the window.
   * @param opts The window options.
   * @param appLogic The application logic.
   */
  public Engine(String windowTitle, WindowOptions opts, EngineLogic appLogic) {
    this.window = new Window(windowTitle, opts, () -> {
      this.resize();
      return null;
    });
    this.targetFps = opts.fps;
    this.targetUps = opts.ups;
    this.appLogic = appLogic;
    this.render = new Render();
    this.scene = new GLScene( new Scene(this.window.getWidth(), this.window.getHeight()));
    this.appLogic.init(this.window, this.scene, this.render);
    this.running = true;
  }

  /**
   * Sets the render options.
   * Must be called before render is called.
   * @param options The render options.
   */
  public void setRenderOptions(RenderOptions options){
    this.render.setOptions(options);
  }


/**
   * Refreshes the engine.
   * Is called when the engine is stopped and has to be ready to start again.
   */
  public void refresh() {
    this.appLogic.cleanup();
    this.render.cleanup();
    this.scene.cleanup();
  }


  /**
   * Releases all resources and terminates the engine.
   * Is called when the engine is stopped and all resources have to be released.
   */
  public void clear() {
    // Calls the registered app for cleaning up
    this.appLogic.cleanup();
    this.render.cleanup();
    this.scene.cleanup();
    this.window.cleanup();
  }

  /**
   * Starts the engine in continuous rendering mode.
   */
  public void start() {
    this.running = true;
    this.run();
  }

  /**
   * Stops the continuous rendering mode.
   */
  public void stop() {
    this.running = false;
  }

  /**
   * Runs a single frame rendering.
   */
  public void runOnce() {
    this.window.pollEvents();
    this.appLogic.beforeRender(this.window, this.scene, this.render);
    this.render.render(this.window, this.scene);
    this.appLogic.afterRender(this.window, this.scene, this.render);
    this.window.update();
  }

  /**
   * Run mode runs permanently until the engine is stopped.
   * 1. Poll events
   * 2. Input
   * 3. Update
   * 4. Render
   * 5. Update window
   */
  public void run() {
    var initialTime = System.currentTimeMillis();
    //  maximum elapsed time between updates
    var timeU = 1000.0f / this.targetUps;
    // maximum elapsed time between render calls
    var timeR = this.targetFps > 0 ? 1000.0f / this.targetFps : 0;
    var deltaUpdate = 0.0f;
    var deltaFps = 0.0f;

    var updateTime = initialTime;

    while (this.running && !this.window.windowShouldClose()) {
      this.window.pollEvents();

      var now = System.currentTimeMillis();

      // relation between actual and elapsed time. 1 if equal.
      deltaUpdate += (now - initialTime) / timeU;
      deltaFps += (now - initialTime) / timeR;

      // If passed maximum elapsed time for render, process user input
      if (this.targetFps <= 0 || deltaFps >= 1) {
        this.appLogic.input(this.window, this.scene, now - initialTime);
      }

      // If passed maximum elapsed time for update, update the scene
      if (deltaUpdate >= 1) {
        var diffTimeMillis = now - updateTime;
        this.appLogic.update(this.window, this.scene, diffTimeMillis);
        updateTime = now;
        deltaUpdate--;
      }

      // If passed maximum elapsed time for render, render the scene
      if (this.targetFps <= 0 || deltaFps >= 1) {
        this.appLogic.beforeRender(this.window, this.scene, this.render);
        this.render.render(this.window, this.scene);
        deltaFps--;
        this.window.update();
        this.appLogic.afterRender(this.window, this.scene, this.render);
      }
    }
    this.refresh();
  }

  /**
   * Resizes the window.
   */
  public void resize() {
    this.scene.resize(this.window.getWidth(), this.window.getHeight());
  }

  /**
   * Returns the camera object.
   * @return The camera object.
   */
  public Camera getCamera() {
    return this.scene.getCamera();
  }

  /**
   * Returns the window object.
   * @return The window object.
   */
  public Window getWindow() {
    return this.window;
  }

  /**
   * Returns the scene object.
   * @return The scene object.
   */
  public GLScene getScene() {
    return this.scene;
  }
}
