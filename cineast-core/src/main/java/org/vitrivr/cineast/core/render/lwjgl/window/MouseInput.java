package org.vitrivr.cineast.core.render.lwjgl.window;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class MouseInput {

  private final Vector2f currentPos;
  private final Vector2f previousPos;
  private final Vector2f displacementVector;
  private boolean inWindow;
  private boolean leftButtonPressed;
  private boolean rightButtonPressed;

  public MouseInput(long windowHandle) {
    this.previousPos = new Vector2f(-1, -1);
    this.currentPos = new Vector2f();
    this.displacementVector = new Vector2f();
    this.leftButtonPressed = false;
    this.rightButtonPressed = false;
    this.inWindow = false;

    GLFW.glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
      this.currentPos.x = (float) xpos;
      this.currentPos.y = (float) ypos;
    });
    GLFW.glfwSetCursorEnterCallback(windowHandle, (window, entered) -> {
      this.inWindow = entered;
    });
    GLFW.glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
      this.leftButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS;
      this.rightButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS;
    });
  }

  public Vector2f getDisplacementVector() {
    return this.displacementVector;
  }

  public Vector2f getCurrentPos() {
    return this.currentPos;
  }

  public void input() {
    this.displacementVector.x = 0f;
    this.displacementVector.y = 0f;

    if (this.previousPos.x > 0f && this.previousPos.y > 0f && this.inWindow) {
      var dX = this.currentPos.x - this.previousPos.x;
      var dY = this.currentPos.y - this.previousPos.y;
      var rX = dX != 0;
      var rY = dY != 0;
      if (rX) {
        this.displacementVector.x = dX;
      }
      if (rY) {
        this.displacementVector.y = dY;
      }
    }
    this.previousPos.x = this.currentPos.x;
    this.previousPos.y = this.currentPos.y;
  }
  public boolean isLeftButtonPressed() {
    return this.leftButtonPressed;
  }
  public boolean isRightButtonPressed() {
    return this.rightButtonPressed;
  }
}
