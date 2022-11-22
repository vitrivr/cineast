package org.vitrivr.cineast.core.render.lwjgl.engine;


import org.vitrivr.cineast.core.render.lwjgl.render.Render;
import org.vitrivr.cineast.core.render.lwjgl.scene.Camera;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GlScene;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;


public class Engine {

  public static final int TARGET_UPS = 30;

  private final Window window;
  private boolean running;
  private final Render render;
  private final GlScene scene;
  private final IEngineLogic appLogic;
  private final int targetFps;
  private final int targetUps;


  public Engine(String windowTitle, WindowOptions opts, IEngineLogic appLogic) {
    this.window = new Window(windowTitle, opts, () -> {
      this.resize();
      return null;
    });
    this.targetFps = opts.fps;
    this.targetUps = opts.ups;
    this.appLogic = appLogic;
    this.render = new Render();
    this.scene = new GlScene( new Scene(this.window.getWidth(), this.window.getHeight()));
    this.appLogic.init(this.window, this.scene, this.render);
    this.running = true;
  }

  private void cleanup() {
    this.appLogic.cleanup();
    this.render.cleanup();
    this.scene.cleanup();
    this.window.cleanup();
  }

  public void start() {
    this.running = true;
    this.run();
  }

  public void stop() {
    this.running = false;
  }

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

      // relation betwwen actual and elapsed time. 1 if equal.
      deltaUpdate += (now - initialTime) / timeU;
      deltaFps += (now - initialTime) / timeR;

      // If passed maximum elapsed time for render, process user input
      if (this.targetFps <= 0 || deltaFps >= 1) {
        this.appLogic.input(this.window, this.scene, now - initialTime);
      }

      if (deltaUpdate >= 1) {
        var diffTimeMillis = now - updateTime;
        this.appLogic.update(this.window, this.scene, diffTimeMillis);
        updateTime = now;
        deltaUpdate--;
      }

      if (this.targetFps <= 0 || deltaFps >= 1) {
        this.appLogic.beforeRender(this.window, this.scene, this.render);
        this.render.render(this.window, this.scene);
        deltaFps--;
        this.window.update();
        this.appLogic.afterRender(this.window, this.scene, this.render);
      }
    }
    this.cleanup();
  }

  public void resize() {
    this.scene.resize(this.window.getWidth(), this.window.getHeight());
  }

  public void clear() {
    this.scene.clearModels();
  }

  public Camera getCamera() {
    return this.scene.getCamera();
  }

  public Window getWindow() {
    return this.window;
  }

  public GlScene getScene() {
    return this.scene;
  }
}
