package org.vitrivr.cineast.core.render.lwjgl.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.render.Renderer;
import org.vitrivr.cineast.core.render.lwjgl.engine.Engine;
import org.vitrivr.cineast.core.render.lwjgl.engine.IEngineLogic;
import org.vitrivr.cineast.core.render.lwjgl.model.Entity;
import org.vitrivr.cineast.core.render.lwjgl.model.IModel;
import org.vitrivr.cineast.core.render.lwjgl.model.Model;
import org.vitrivr.cineast.core.render.lwjgl.render.Render;
import org.vitrivr.cineast.core.render.lwjgl.scene.LightfieldCamera;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;

public class LWJGLOffscreenRenderer implements Renderer, IEngineLogic {

  private static final Logger LOGGER = LogManager.getLogger();

  private ReentrantLock lock = new ReentrantLock(true);

  public final WindowOptions windowOptions;
  private static LWJGLOffscreenRenderer instance;
  Engine engine;

  private LinkedTransferQueue<IModel> modelQueue;

  public LWJGLOffscreenRenderer(WindowOptions opts) {
    if (LWJGLOffscreenRenderer.instance != null) {
      throw new IllegalStateException("Engine is already running.");
    }
    this.windowOptions = opts;
    this.modelQueue = new LinkedTransferQueue<IModel>();
    LWJGLOffscreenRenderer.instance = this;
    var name = "LWJGLOffscreenRenderer";
    this.engine = new Engine(name, this.windowOptions, LWJGLOffscreenRenderer.instance);
    LOGGER.info("LWJGLOffscreenRenderer created");
  }

  public LWJGLOffscreenRenderer(int width, int height) {
    this(new WindowOptions() {{
      this.width = width;
      this.height = height;
    }});
  }

  @Override
  public void render() {
    this.engine.runOnce();
    LOGGER.info("LWJGLOffscreenRenderer started");
  }

  public void cleanup() {
    LOGGER.info("LWJGLOffscreenRenderer cleanup");
  }

  //
  public void init(Window window, Scene scene, Render render) {
    scene.getCamera().setPosition(0, 0, 1);
  }

  /**
   * This method is called every frame.
   *
   * @param window
   * @param scene
   * @param diffTimeMillis
   */
  public void input(Window window, Scene scene, long diffTimeMillis) {
    var cam = scene.getCamera();
    cam.moveOrbit(0.1f, 0.0f, 0.0f);
    scene.getModels().forEach((k, v) -> v.getEntities().forEach(Entity::updateModelMatrix));
  }

  /**
   * After Engine run This method is called every frame.
   *
   * @param window
   * @param scene
   * @param diffTimeMillis
   */
  public void update(Window window, Scene scene, long diffTimeMillis) {

    if (!this.modelQueue.isEmpty()) {
      var model = (Model) this.modelQueue.poll();
      if (model.getEntities().size() == 0) {
        var entity = new Entity("cube", model.getId());
        entity.setPosition(0, 0, 0);
        entity.setScale(1f);
        model.getEntities().add(entity);
      }

      scene.clearModels();
      scene.addModel(model);
    }
    scene.getModels().forEach((k, v) -> v.getEntities().forEach(Entity::updateModelMatrix));
  }


  public int getWidth() {
    return this.windowOptions.width;
  }

  public int getHeight() {
    return this.windowOptions.height;
  }

  public float getAspect() {
    return (float) this.windowOptions.width / (float) this.windowOptions.height;
  }


  @Override
  public void assemble(IModel model) {
    this.modelQueue.add(model);
  }

  @Override
  public BufferedImage obtain() {
    this.engine.runOnce();
    var cam = new LightfieldCamera(this.windowOptions, "Test");
    return cam.takeLightfieldImage();
  }

  @Override
  public void clear(Color color) {

  }

  @Override
  public void clear() {

  }

  @Override
  public boolean retain() {
    return this.lock.tryLock();
  }

  @Override
  public void release() {
    this.lock.unlock();
  }

  @Override
  public void positionCamera(double ex, double ey, double ez, double cx, double cy, double cz, double upx, double upy,
      double upz) {

  }


}
