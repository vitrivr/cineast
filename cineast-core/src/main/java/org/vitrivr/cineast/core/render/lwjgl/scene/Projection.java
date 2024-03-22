package org.vitrivr.cineast.core.render.lwjgl.scene;

import org.joml.Matrix4f;

/**
 * The Projection class is used to create a projection matrix.
 * The projection matrix is used to transform the 3D scene into a 2D image.
 */
public class Projection {

  /**
   * The FOV is the field of view of the camera.
   */
  private static final float FOV = (float) Math.toRadians(60.0f);
  /**
   * The Z_FAR and Z_NEAR values are used to set the clipping planes.
   */
  private static final float Z_FAR = 100.f;
  /**
   * The Z_FAR and Z_NEAR values are used to set the clipping planes.
   */
  private static final float Z_NEAR = 0.01f;

  /**
   * The projMatrix for rendering the scene.
   */
  private final Matrix4f projMatrix;

  /**
   * Initializes the Projection with the given width and height.
   * Creates a new projection matrix.
   *
   * @param width  The width of the window.
   * @param height The height of the window.
   */
  public Projection(int width, int height) {
    this.projMatrix = new Matrix4f();
    this.updateProjMatrix(width, height);
  }

  /**
   * Returns the projection matrix.
   *
   * @return The projection matrix.
   */
  public Matrix4f getProjMatrix() {
    return this.projMatrix;
  }

  /**
   * Updates the projection matrix.
   *
   * @param width  The width of the window.
   * @param height The height of the window.
   */
  public void updateProjMatrix(int width, int height) {
    this.projMatrix.setPerspective(FOV, (float) width / (float) height, Z_NEAR, Z_FAR);
  }
}
