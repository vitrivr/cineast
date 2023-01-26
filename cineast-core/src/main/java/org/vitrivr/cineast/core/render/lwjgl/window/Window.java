package org.vitrivr.cineast.core.render.lwjgl.window;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import java.util.concurrent.Callable;


import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;

import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

  private static final Logger LOGGER = LogManager.getLogger();
  private final long windowHandle;
  private int height;
  private final Callable<Void> resizeFunc;
  private int width;
  private final MouseInput mouseInput;

  public Window(String title, WindowOptions opts, Callable<Void> resizeFunc) {

    this.resizeFunc = resizeFunc;
    LOGGER.trace("Try creating window '{}'...", title);
    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }
    LOGGER.trace("GLFW initialized");

    GLFW.glfwDefaultWindowHints();
    // Window should be invisible for basic rendering
    GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL30.GL_FALSE);
    // Setting for headless rendering with MESA and Xvfb
    // See: https://github.com/vitrivr/cineast/blob/e5587fce1b5675ca9f6dbbfd5c17eb1880a98ce3/README.md
    //GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_OSMESA_CONTEXT_API);
    // Switch off resize callback
    GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL30.GL_FALSE);

    // Sets the OpenGL version number to MAJOR.MINOR e.g. 3.2
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);

    // Depending on the Options, the OpenGL profile can be set to either CORE or COMPAT (or ANY)
    // GLFW_OPENGL_COMPAT_PROFILE keeps the outdated functionality
    // GLFW_OPENGL_CORE_PROFILE removes the deprecated functionality
    // GLFW_OPENGL_ANY_PROFILE is used for version 3.2 and below
    if (opts.compatibleProfile) {
      GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
    } else {
      GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
      GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL30.GL_TRUE);
    }

    // Set window size if set in options, otherwise use maximum size of primary monitor
    if (opts.width > 0 && opts.height > 0) {
      this.width = opts.width;
      this.height = opts.height;
    } else {
      GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);
      var vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
      assert vidMode != null;
      this.width = vidMode.width();
      this.height = vidMode.height();

    }

    LOGGER.trace("Try creating window '{}' with size {}x{}...", title, this.width, this.height);
    this.windowHandle = GLFW.glfwCreateWindow(this.width, this.height, title, NULL, NULL);
    if (this.windowHandle == NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    // Setup the callbacks for the glfw window.
    // Resize and key Callback are not used for headless rendering.
    //
    var resizeCallback = GLFW.glfwSetFramebufferSizeCallback(this.windowHandle, (window, w, h) -> this.resized(w, h));

    var errorCallback = GLFW.glfwSetErrorCallback((int errorCode, long msgPtr) ->
        LOGGER.error("Error code [{}], msg [{}]", errorCode, MemoryUtil.memUTF8(msgPtr))
    );

    var keyCallback = GLFW.glfwSetKeyCallback(this.windowHandle, (window, key, scancode, action, mods) -> {
      if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
        GLFW.glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
      }
    });

    GLFW.glfwMakeContextCurrent(this.windowHandle);

    if (opts.fps > 0) {
      GLFW.glfwSwapInterval(0);
    } else {
      GLFW.glfwSwapInterval(1);
    }

    if (!opts.hideWindow) {
      GLFW.glfwShowWindow(this.windowHandle);
    }

    var arrWidth = new int[1];
    var arrHeight = new int[1];
    GLFW.glfwGetFramebufferSize(this.windowHandle, arrWidth, arrHeight);
    this.width = arrWidth[0];
    this.height = arrHeight[0];

    this.mouseInput = new MouseInput(this.windowHandle);
  }

  /**
   * Removes all callbacks and destroys the window.
   */
  public void cleanup() {
    glfwFreeCallbacks(this.windowHandle);
    GLFW.glfwDestroyWindow(this.windowHandle);
    GLFW.glfwTerminate();
    var callback = GLFW.glfwSetErrorCallback(null);
    if (callback != null) {
      callback.free();
    }
  }

  public int getHeight() {
    return this.height;
  }

  public int getWidth() {
    return this.width;
  }

  public boolean isKeyPressed(int keyCode) {
    return GLFW.glfwGetKey(this.windowHandle, keyCode) == GLFW.GLFW_PRESS;
  }

  public void pollEvents() {
    GLFW.glfwPollEvents();
    this.mouseInput.input();
  }

  protected void resized(int width, int height) {
    this.width = width;
    this.height = height;
    try {
      this.resizeFunc.call();
    } catch (Exception excp) {
      LOGGER.error("Error calling resize callback", excp);
    }
  }

  public void update() {
    GLFW.glfwSwapBuffers(this.windowHandle);
  }

  public boolean windowShouldClose() {
    return GLFW.glfwWindowShouldClose(this.windowHandle);
  }

  public MouseInput getMouseInput() {
    return this.mouseInput;
  }
}