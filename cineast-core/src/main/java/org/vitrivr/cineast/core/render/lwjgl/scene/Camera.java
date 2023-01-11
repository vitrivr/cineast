package org.vitrivr.cineast.core.render.lwjgl.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

  private Vector3f translativX;
  private Vector3f translativY;
  private Vector3f translativZ;
  private Vector3f position;
  private Vector2f rotation;
  private Matrix4f viewMatrix;

  private Quaternionf orbitRotation;

  public Camera() {
    this.translativZ = new Vector3f();
    this.translativX = new Vector3f();
    this.translativY = new Vector3f();
    this.position = new Vector3f();
    this.viewMatrix = new Matrix4f();
    this.rotation = new Vector2f();
    this.orbitRotation = new Quaternionf();
  }

  public void rotate(float x, float y) {
    this.rotation.add(x, y);
    this.recalculate();
  }

  public Vector3f getPosition() {
    return this.position;
  }

  public Matrix4f getViewMatrix() {
    return this.viewMatrix;
  }

  private void move(float x, float y, float z) {
    this.position.add(x, y, z);
    if (x > 0) {
      this.move(x, Direction.RIGHT);
    } else if (x < 0) {
      this.move(x, Direction.LEFT);
    }
    if (y > 0) {
      this.move(y, Direction.UP);
    } else if (y < 0) {
      this.move(y, Direction.DOWN);
    }
    if (z > 0) {
      this.move(z, Direction.FORWARD);
    } else if (z < 0) {
      this.move(z, Direction.BACKWARD);
    }
  }

  public void move(float inc, Direction direction) {
    switch (direction) {
      case FORWARD -> {
        this.viewMatrix.positiveZ(this.translativZ).negate().mul(inc);
        this.position.add(this.translativZ);
      }
      case BACKWARD -> {
        this.viewMatrix.positiveZ(this.translativZ).negate().mul(inc);
        this.position.sub(this.translativZ);
      }
      case LEFT -> {
        this.viewMatrix.positiveX(this.translativX).mul(inc);
        this.position.sub(this.translativX);
      }
      case RIGHT -> {
        this.viewMatrix.positiveX(this.translativX).mul(inc);
        this.position.add(this.translativX);
      }
      case UP -> {
        this.viewMatrix.positiveY(this.translativY).mul(inc);
        this.position.add(this.translativY);
      }
      case DOWN -> {
        this.viewMatrix.positiveY(this.translativY).mul(inc);
        this.position.sub(this.translativY);
      }
    }
    this.recalculate();
  }

  private void recalculate() {
    this.viewMatrix.identity()
        .rotate(this.orbitRotation)
        .translate(-this.position.x, -this.position.y, -this.position.z);

  }

  public Camera setPosition(float x, float y, float z) {
    this.position.set(x, y, z);
    this.recalculate();
    return this;
  }

  public Camera setPosition(Vector3f position) {
    this.setPosition(position.x, position.y, position.z);
    return this;
  }

  public Camera setRotation(float x, float y) {
    this.rotation.set(x, y);
    this.recalculate();
    return this;
  }

  public Camera moveOrbit(float x, float y, float z) {
    y = (float) ((double) y * 2.0 * Math.PI);
    x = (float) ((double) x * 2.0 * Math.PI);
    z = (float) ((double) z * 2.0 * Math.PI);
    this.orbitRotation.rotateYXZ(y, x, z);
    //this.viewMatrix.rotate(this.orbitRotation);
    this.recalculate();
    return this;
  }

  public Camera setOrbit(float x, float y, float z) {
    x = (float) ((double) x * 2.0 * Math.PI);
    y = (float) ((double) y * 2.0 * Math.PI);
    z = (float) ((double) z * 2.0 * Math.PI);
    //this.orbitRotation.fromAxisAngleRad(x, y, z, angle);
    this.orbitRotation.rotationXYZ(x, y, z);
    //this.viewMatrix.rotate(this.orbitRotation);
    this.recalculate();
    return this;
  }

  public Camera setPositionAndOrientation(Vector3f cameraPosition, Vector3f objectPosition) {
    var lookDir = new Vector3f(objectPosition).sub(cameraPosition).normalize();
    var yNorm = new Vector3f(0, 1, 0).normalize();
    var right = new Vector3f(lookDir).cross(yNorm).normalize();
    if ( Float.isNaN(right.x()) || Float.isNaN(right.y())|| Float.isNaN(right.y())) {
      right = new Vector3f(1, 0, 0);
    }
    var up = new Vector3f(right).cross(lookDir).normalize();
    this.position.set(cameraPosition);
    this.orbitRotation = new Quaternionf();
    this.orbitRotation.lookAlong(lookDir, up);
    this.recalculate();
    return this;
  }

  public Camera setPositionAndOrientation(Vector3f cameraPosition, Vector3f objectPosition, Vector3f up) {
    var lookDir = new Vector3f(objectPosition).sub(cameraPosition).normalize();

    this.setPosition(cameraPosition);
    this.orbitRotation.lookAlong(lookDir, up);
    this.recalculate();
    return this;
  }


  public float degreHandler(float degre) {
    if (degre > 360) {
      degre -= 360;
    } else if (degre < 0) {
      degre += 360;
    }
    return degre;
  }

}
