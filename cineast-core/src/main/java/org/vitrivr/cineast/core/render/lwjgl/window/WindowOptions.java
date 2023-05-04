package org.vitrivr.cineast.core.render.lwjgl.window;


/**
 * This class holds all options for a window.
 */
public class WindowOptions {

  /**
   * set to true if the window should be created with a compatible profile
   */
  public boolean compatibleProfile;
  /**
   * Frames per second. If set to -1, the fps is unlimited.
   */
  public int fps = -1;
  /**
   * Updates per second. If set to -1, the ups is unlimited.
   */
  public int ups = 30;
  /**
   * The height of the window.
   */
  public int height = 400;
  /**
   * The width of the window.
   */
  public int width = 400;
  /**
   * Hide the window after creation.
   */
  public boolean hideWindow = true;
  /**
   * Empty constructor for WindowOptions.
   */
  public WindowOptions(){}

  /**
   * Basic Constructor for WindowOptions.
   *
   * @param width  Width of the window.
   * @param height Height of the window.
   */
  public WindowOptions(int width, int height) {
    this.width = width;
    this.height = height;
  }
}
