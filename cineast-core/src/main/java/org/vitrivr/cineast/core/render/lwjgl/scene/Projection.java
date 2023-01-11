package org.vitrivr.cineast.core.render.lwjgl.scene;

import org.joml.Matrix4f;

public class Projection {

  private static final float FOV = (float) Math.toRadians(60.0f);
  private static final float Z_FAR = 100.f;
  private static final float Z_NEAR = 0.01f;

  private final Matrix4f projMatrix;

  public Projection(int width, int height) {
    this.projMatrix = new Matrix4f();
    this.updateProjMatrix(width, height);
  }

  public Matrix4f getProjMatrix() {
    return this.projMatrix;
  }

  public void updateProjMatrix(int width, int height) {
      this.projMatrix.setPerspective(FOV, (float) width / (float) height, Z_NEAR, Z_FAR);
  }
}
