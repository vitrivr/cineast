package org.vitrivr.cineast.core.render.lwjgl.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedTransferQueue;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.render.lwjgl.engine.Engine;
import org.vitrivr.cineast.core.render.lwjgl.engine.IEngineLogic;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Model;
import org.vitrivr.cineast.core.render.lwjgl.render.Render;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GlScene;
import org.vitrivr.cineast.core.render.lwjgl.scene.LightfieldCamera;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class LWJGLOffscreenRenderer extends IEngineLogic implements IRenderer {

  private static final Logger LOGGER = LogManager.getLogger();


  public WindowOptions windowOptions;
  Engine engine;

  private LinkedTransferQueue<IModel> modelQueue;
  private LinkedTransferQueue<BufferedImage> imageQueue;



  public LWJGLOffscreenRenderer() {
    this.modelQueue = new LinkedTransferQueue<IModel>();
    this.imageQueue = new LinkedTransferQueue<BufferedImage>();
    LOGGER.info("LWJGLOffscreenRenderer created");
  }


  public void setWindowOptions(WindowOptions opts){
    this.windowOptions = opts;
  }

  public void startEngine(){
    var name = "LWJGLOffscreenRenderer";
    this.engine = new Engine(name, this.windowOptions, this);
  }

  @Override
  public void render() {
    this.engine.runOnce();
    LOGGER.info("LWJGLOffscreenRenderer rendered");
  }


  protected void cleanup() {
    LOGGER.info("LWJGLOffscreenRenderer cleaned");
  }

  /**
   * Is called once at the initialization of the engine.
   *
   * @param window
   * @param scene
   * @param render
   */
  @Override
  protected void init(Window window, GlScene scene, Render render) {
    scene.getCamera().setPosition(0, 0, 5);
  }

  @Override
  protected void beforeRender(Window window, GlScene scene, Render render) {
    this.loadNextModelFromQueueToScene(window, scene);
  }

  @Override
  protected void afterRender(Window window, GlScene scene, Render render) {
    var lfc = new LightfieldCamera(this.windowOptions);
    this.imageQueue.add(lfc.takeLightfieldImage());
  }

  /**
   * This method is called every frame.
   *
   * @param window
   * @param scene
   * @param diffTimeMillis
   */
  @Override
  protected void input(Window window, GlScene scene, long diffTimeMillis) {
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
  protected void update(Window window, GlScene scene, long diffTimeMillis) {
  }

  private void loadNextModelFromQueueToScene(Window window, GlScene scene) {
    if (!this.modelQueue.isEmpty()) {
      var model = (Model) this.modelQueue.poll();
      if (model.getEntities().size() == 0) {
        var entity = new Entity("cube", model.getId());
        entity.setPosition(0, 0, 0);
        entity.setScale(1f);
        model.addEntityNorm(entity);
      }

      scene.clearModels();
      scene.addModel(model);
    }
    scene.getModels().forEach((k, v) -> v.getEntities().forEach(Entity::updateModelMatrix));
  }

  public void moveCameraOrbit(float x, float y, float z) {
    this.engine.getCamera().moveOrbit(x, y, z);
  }

  public void setCameraOrbit(float x, float y, float z) {
    this.engine.getCamera().setOrbit(x, y, z);
  }

  public void setCameraPosition(float x, float y, float z) {
    this.engine.getCamera().setPosition(x, y, z);
  }


  @Override
  public void positionCamera(double ex, double ey, double ez,
      double cx, double cy, double cz,
      double upx, double upy, double upz) {
    this.engine.getCamera().setPositionAndOrientation(
        new Vector3f((float) ex, (float) ey, (float) ez),
        new Vector3f((float) cx, (float) cy, (float) cz),
        new Vector3f((float) upx, (float) upy, (float) upz));
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

  /**
   * This method disposes the engine.
   * Window is destroyed and all resources are freed.
   */
  @Override
  public void clear() {
    this.engine.clear();
  }

  @Override
  public boolean retain() {
    return true;
  }

  @Override
  public void release() {
    this.engine.clear();
  }

  public int getWidth() {
    return this.windowOptions.width;
  }

  public int getHeight() {
    return this.windowOptions.height;
  }

}
