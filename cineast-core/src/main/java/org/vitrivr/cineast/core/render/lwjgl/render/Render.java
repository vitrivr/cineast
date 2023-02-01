package org.vitrivr.cineast.core.render.lwjgl.render;


import org.vitrivr.cineast.core.render.lwjgl.window.Window;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GlScene;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

public class Render {

  private final SceneRender sceneRender;
  private RenderOptions options;

  public Render() {
    GL.createCapabilities();
    GL30.glEnable(GL30.GL_DEPTH_TEST);
    GL30.glEnable(GL30.GL_CULL_FACE);
    GL30.glCullFace(GL30.GL_BACK);
    this.sceneRender = new SceneRender();
  }

  public void setOptions(RenderOptions options){
    this.options = options;
  }

  public void cleanup() {
    this.sceneRender.cleanup();
    this.options = null;
  }

  public void render(Window window, GlScene scene) {
    GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
    GL30.glViewport(0, 0, window.getWidth(), window.getHeight());

    this.sceneRender.render(scene, this.options);
  }


}
