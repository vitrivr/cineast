package org.vitrivr.cineast.core.render.lwjgl.window;

import org.vitrivr.cineast.core.render.lwjgl.engine.Engine;

public class WindowOptions {

  public boolean compatibleProfile;
  public int fps = -1;
  public int height = 1440;
  public int ups = Engine.TARGET_UPS;
  public int width = 2560;
  public boolean hideWindow = true;
  public WindowOptions(){}

  public WindowOptions(int width, int height) {
    this.width = width;
    this.height = height;
  }
}
