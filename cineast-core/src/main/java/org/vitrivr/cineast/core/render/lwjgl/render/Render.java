package org.vitrivr.cineast.core.render.lwjgl.render;


import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;
import org.vitrivr.cineast.core.render.lwjgl.window.Window;

public class Render {

  private final SceneRender sceneRender;

  public Render() {
    GL.createCapabilities();
    GL30.glEnable(GL30.GL_DEPTH_TEST);
    GL30.glEnable(GL30.GL_CULL_FACE);
    GL30.glCullFace(GL30.GL_BACK);
    this.sceneRender = new SceneRender();
  }

  public void cleanup() {
    this.sceneRender.cleanup();
  }

  public void render(Window window, Scene scene) {
    GLFW.glfwMakeContextCurrent(window.getWindowHandle());
    GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
    GL30.glViewport(0, 0, window.getWidth(), window.getHeight());

    this.sceneRender.render(scene);
  }
}
