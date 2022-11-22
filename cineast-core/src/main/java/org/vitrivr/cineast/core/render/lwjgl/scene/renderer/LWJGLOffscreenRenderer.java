package org.vitrivr.cineast.core.render.lwjgl.scene.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.render.Renderer;
import org.vitrivr.cineast.core.render.lwjgl.engine.Engine;
import org.vitrivr.cineast.core.render.lwjgl.engine.IEngineLogic;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Model;
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
  private LinkedTransferQueue<BufferedImage> imageQueue;

  public LWJGLOffscreenRenderer(WindowOptions opts) {
/*    if (LWJGLOffscreenRenderer.instance != null) {
      throw new IllegalStateException("Engine is already running.");
    }*/
    this.windowOptions = opts;
    this.modelQueue = new LinkedTransferQueue<IModel>();
    this.imageQueue = new LinkedTransferQueue<BufferedImage>();
    //LWJGLOffscreenRenderer.instance = this;
    var name = "LWJGLOffscreenRenderer";
    this.engine = new Engine(name, this.windowOptions, this);
    LOGGER.info("LWJGLOffscreenRenderer created");
  }

  public LWJGLOffscreenRenderer(int width, int height) {
    this(
        new WindowOptions(width, height) {{
          hideWindow = true;
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

  /**
   * Is called once at the initialization of the engine.
   *
   * @param window
   * @param scene
   * @param render
   */
  @Override
  public void init(Window window, Scene scene, Render render) {
    scene.getCamera().setPosition(0, 0, 1);
  }

  @Override
  public void beforeRender(Window window, Scene scene, Render render) {
    this.loadNextModelFromQueueToScene(window, scene);
  }

  @Override
  public void afterRender(Window window, Scene scene, Render render) {
    var cam = new LightfieldCamera(this.windowOptions);
    var img = cam.takeLightfieldImage();
    this.imageQueue.add(img);
  }

  /**
   * This method is called every frame.
   *
   * @param window
   * @param scene
   * @param diffTimeMillis
   */
  @Override
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
  @Override
  public void update(Window window, Scene scene, long diffTimeMillis) {

  }

  private void loadNextModelFromQueueToScene(Window window, Scene scene) {
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


  @Override
  public void positionCamera(double ex, double ey, double ez, double cx, double cy, double cz, double upx, double upy,
      double upz) {
    this.engine.getCamera().setOrbit((float) ex, (float) ey, (float) ez);
    //this.engine.getCamera().setPositionAndOrientation(new Vector3f((float) ex, (float) ey, (float) ez), new Vector3f((float) cx, (float) cy, (float) cz), new Vector3f((float) upx, (float) upy, (float) upz));
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
    return this.imageQueue.poll();
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

  public int getWidth() {
    return this.windowOptions.width;
  }

  public int getHeight() {
    return this.windowOptions.height;
  }
}
