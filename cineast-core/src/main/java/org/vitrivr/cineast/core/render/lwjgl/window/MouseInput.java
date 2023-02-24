package org.vitrivr.cineast.core.render.lwjgl.window;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

/**
 * The MouseInput class is used to get the mouse input from the user. It is used to get the current position of the mouse and the displacement vector.
 * @deprecated This class is deprecated, It is . TODO: discuss weather there could be a usecase for this class in context of cineast.
 */
@Deprecated
public class MouseInput {

  /**
   * The current mouse position
   */
  private final Vector2f currentPos;
  /**
   * The mouse position in the previous frame
   */
  private final Vector2f previousPos;
  /**
   * The displacement vector of the mouse
   */
  private final Vector2f displacementVector;
  /**
   * Boolean that indicates if the mouse is in the window
   */
  private boolean inWindow;
  /**
   * Boolean that indicates if the left mouse button is pressed
   */
  private boolean leftButtonPressed;
  /**
   * Boolean that indicates if the right mouse button is pressed
   */
  private boolean rightButtonPressed;

  /**
   * Initializes the MouseInput class with the given window handle.
   * @param windowHandle The window handle of the window that is used to get the mouse input.
   */
  @SuppressWarnings("resource")
  public MouseInput(long windowHandle) {
    this.previousPos = new Vector2f(-1, -1);
    this.currentPos = new Vector2f();
    this.displacementVector = new Vector2f();
    this.leftButtonPressed = false;
    this.rightButtonPressed = false;
    this.inWindow = false;

    // Register callbacks
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

  /**
   * Returns the displacement vector of the mouse.
   * @return The displacement vector of the mouse.
   */
  public Vector2f getDisplacementVector() {
    return this.displacementVector;
  }

  /**
   * Returns the current position of the mouse.
   * @return The current position of the mouse.
   */
  public Vector2f getCurrentPos() {
    return this.currentPos;
  }

  /**
   * Updates the mouse input.
   */
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

  /**
   * Returns true if the left mouse button is pressed.
   * @return True if the left mouse button is pressed.
   */
  public boolean isLeftButtonPressed() {
    return this.leftButtonPressed;
  }

  /**
   * Returns true if the right mouse button is pressed.
   * @return True if the right mouse button is pressed.
   */
  public boolean isRightButtonPressed() {
    return this.rightButtonPressed;
  }
}
