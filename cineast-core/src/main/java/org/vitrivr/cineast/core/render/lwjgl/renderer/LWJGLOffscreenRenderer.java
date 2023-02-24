package org.vitrivr.cineast.core.render.lwjgl.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedTransferQueue;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.render.lwjgl.engine.Engine;
import org.vitrivr.cineast.core.render.lwjgl.engine.IEngineLogic;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
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
import org.vitrivr.cineast.core.render.Renderer;

/**
 * This is the top most class of the LWJGL for Java 3D renderer. Its main function is to provide an interface between Engine and the outside world. It sets up the  {@link Engine} and provides the interface to the outside world. {@link Renderer} It extends the abstract class {@link IEngineLogic} which allows the instanced engine to call methods depending on the engine state.
 */
@SuppressWarnings("deprecation")
public class LWJGLOffscreenRenderer extends IEngineLogic implements Renderer {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * The (offscreen) window options for the engine.
   */
  private WindowOptions windowOptions;
  /**
   * The engine instance.
   */
  private Engine engine;

  /**
   * The model queue. From this queue the renderer takes the next model to render.
   */
  private final LinkedTransferQueue<IModel> modelQueue;

  /**
   * The image queue. In this queue the renderer puts the rendered images.
   */
  private final LinkedTransferQueue<BufferedImage> imageQueue;


  /**
   * Constructor for the LWJGLOffscreenRenderer. Initializes the model queue and the image queue.
   */
  public LWJGLOffscreenRenderer() {
    this.modelQueue = new LinkedTransferQueue<>();
    this.imageQueue = new LinkedTransferQueue<>();
    LOGGER.trace("LWJGLOffscreenRenderer created");
  }

  /**
   * Sets the window options for the engine.
   *
   * @param opts The window options.
   */
  public void setWindowOptions(WindowOptions opts) {
    this.windowOptions = opts;
  }

  /**
   * Sets the render options for the engine.
   *
   * @param opts The render options.
   */
  public void setRenderOptions(RenderOptions opts) {
    this.engine.setRenderOptions(opts);
  }

  /**
   * Starts the engine with given window options. Registers the LWJGLOffscreenRenderer as the engine logic.
   */
  public void startEngine() {
    var name = "LWJGLOffscreenRenderer";
    this.engine = new Engine(name, this.windowOptions, this);
  }

  /**
   * Starts the rendering process.
   */
  @Override
  public void render() {
    this.engine.runOnce();
    LOGGER.trace("LWJGLOffscreenRenderer rendered");
  }


  /**
   * Is called once at the initialization of the engine. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
   */
  @Override
  protected void init(Window window, GlScene scene, Render render) {
    scene.getCamera().setPosition(0, 0, 1);
  }

  /**
   * Is called from the engine before the render method. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
   */
  @Override
  protected void beforeRender(Window window, GlScene scene, Render render) {
    this.loadNextModelFromQueueToScene(window, scene);
  }

  /**
   * Is called from the engine after the render method. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
   */
  @Override
  protected void afterRender(Window window, GlScene scene, Render render) {
    var lfc = new LightfieldCamera(this.windowOptions);
    this.imageQueue.add(lfc.takeLightfieldImage());
  }

  /**
   * Is called from the engine as first step during refresh and cleanup DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
   */
  @Override
  protected void cleanup() {
    LOGGER.trace("LWJGLOffscreenRenderer cleaned");
  }


  /**
   * This method is called every frame. This is only used in continuous rendering. The purpose is to do some input handling. Could be use for optimize view  angles on a fast manner. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
   */
  @Override
  protected void input(Window window, GlScene scene, long diffTimeMillis) {
    scene.getModels().forEach((k, v) -> v.getEntities().forEach(Entity::updateModelMatrix));
  }

  /**
   * After Engine run This method is called every frame. This is only used in continuous rendering. The purpose is to process some life output. Could be use for optimize view  angles on a fast manner. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
   */
  @Override
  protected void update(Window window, GlScene scene, long diffTimeMillis) {
  }

  /**
   * This method is called to load the next model into the provided scene.
   *
   * @param scene The scene to put the model in.
   */
  @SuppressWarnings("unused")
  private void loadNextModelFromQueueToScene(Window window, GlScene scene) {
    if (!this.modelQueue.isEmpty()) {
      var model = (Model) this.modelQueue.poll();
      if (model.getEntities().size() == 0) {
        var entity = new Entity("cube", model.getId());
        model.addEntityNorm(entity);
      }
      //cleans all current models from the scene
      scene.cleanup();
      //adds the new model to the scene
      scene.addModel(model);
    }
    scene.getModels().forEach((k, v) -> v.getEntities().forEach(Entity::updateModelMatrix));
  }

  /**
   * Moves the camera in the scene with given deltas in cartesian coordinates. Look at the origin.
   */
  public void moveCameraOrbit(float dx, float dy, float dz) {
    this.engine.getCamera().moveOrbit(dx, dy, dz);
  }

  /**
   * Sets the camera in the scene to cartesian coordinates. Look at the origin.
   */
  public void setCameraOrbit(float x, float y, float z) {
    this.engine.getCamera().setOrbit(x, y, z);
  }

  /**
   * Moves the camera in the scene with given deltas in cartesian coordinates. Keep the orientation.
   */
  @SuppressWarnings("unused")
  public void setCameraPosition(float x, float y, float z) {
    this.engine.getCamera().setPosition(x, y, z);
  }

  /**
   * Set position of the camera and look at the origin. Camera will stay aligned to the y plane.
   */
  public void lookFromAtO(float x, float y, float z) {
    var lookFrom = new Vector3f(x, y, z);
    var lookAt = new Vector3f(0, 0, 0);

    this.engine.getCamera().setPositionAndOrientation(lookFrom, lookAt);

  }

  /**
   * Set position and orientation of the camera.
   *
   * @deprecated Old renderer implementation. Not needed. Use quaternion instead.
   */
  @Override
  public void positionCamera(double ex, double ey, double ez,
      double cx, double cy, double cz,
      double upx, double upy, double upz) {
    this.engine.getCamera().setPositionAndOrientation(
        new Vector3f((float) ex, (float) ey, (float) ez),
        new Vector3f((float) cx, (float) cy, (float) cz),
        new Vector3f((float) upx, (float) upy, (float) upz));
  }


  /**
   * Returns the aspect ratio of the window.
   */
  @SuppressWarnings("unused")
  public float getAspect() {
    return (float) this.windowOptions.width / (float) this.windowOptions.height;
  }

  /**
   * Interface to outside to add a model to the scene.
   */
  @Override
  public void assemble(IModel model) {
    this.modelQueue.add(model);
  }

  /**
   * Interface to outside to get a rendered image.
   */
  @Override
  public BufferedImage obtain() {
    return this.imageQueue.poll();
  }

  /**
   * This method disposes the engine. Window is destroyed and all resources are freed.
   */
  @Override
  public void clear(Color color) {
    this.clear();
  }

  /**
   * This method disposes the engine. Window is destroyed and all resources are freed.
   */
  @Override
  public void clear() {
    this.engine.clear();
    this.engine = null;
  }

  /**
   * Retains control of the Renderer. While a Thread retains a renderer, no other thread should be allowed to use it!
   *
   * @deprecated Old renderer implementation. Indicates that the renderer should be retained.
   */
  @Override
  public boolean retain() {
    return true;
  }

  /**
   * Releases control of the Renderer, making it usable by other Threads again.
   *
   * @deprecated Old renderer implementation. Indicates that the renderer should be retained.
   */
  @Override
  public void release() {
    this.engine.clear();
  }

  /**
   * Returns the width of the window.
   *
   * @return The width of the window. (in pixels)
   */
  public int getWidth() {
    return this.windowOptions.width;
  }

  /**
   * Returns the height of the window.
   *
   * @return The height of the window. (in pixels)
   */
  public int getHeight() {
    return this.windowOptions.height;
  }
}
