package org.vitrivr.cineast.core.render.lwjgl.render;

import org.vitrivr.cineast.core.render.lwjgl.window.Window;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GlScene;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

/**
 * This class holds the render logic for the LWJGL engine
 * Holds the {@link SceneRender} which loads shaders
 */
public class Render {

  private final SceneRender sceneRender;
  private RenderOptions options;

  /**
   * Create a render instance Set up the Render options for OpenGL
   */
  public Render() {
    GL.createCapabilities();
    GL30.glEnable(GL30.GL_DEPTH_TEST);
    GL30.glEnable(GL30.GL_CULL_FACE);
    GL30.glCullFace(GL30.GL_BACK);
    this.sceneRender = new SceneRender();
  }

  /**
   * Set the render options {@link RenderOptions}
   *
   * @param options see {@link RenderOptions}
   */
  public void setOptions(RenderOptions options) {
    this.options = options;
  }

  /**
   * Releases all resources
   */
  public void cleanup() {
    this.sceneRender.cleanup();
    this.options = null;
  }

  /**
   * Renders a given Scene in a Given Window
   *
   * @param window GL (offscreen) window instance {@link Window}
   * @param scene  GL Scene (containing all models) {@link GlScene}
   */
  public void render(Window window, GlScene scene) {
    GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
    GL30.glViewport(0, 0, window.getWidth(), window.getHeight());
    this.sceneRender.render(scene, this.options);
  }
}
