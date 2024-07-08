package org.vitrivr.cineast.core.render.lwjgl.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Camera class for the LWJGL renderer.
 * <p>
 * This class is responsible for the camera position and orientation. It is used to calculate the view matrix for the renderer. It provides methods to move and rotate the camera.
 */
public class Camera implements ILocateable {

  /**
   * Helper Vector for X-Axis translation.
   */
  private final Vector3f translativX;
  /**
   * Helper Vector for Y-Axis translation.
   */
  private final Vector3f translativY;
  /**
   * Helper Vector for Z-Axis translation.
   */
  private final Vector3f translativZ;
  /**
   * Position of the camera.
   */
  private final Vector3f position;
  /**
   * Rotation of the camera.
   */
  private final Vector2f rotation;
  /**
   * Resulting view matrix.
   */
  private final Matrix4f viewMatrix;
  /**
   * Helper Quaternion for orbit rotation.
   */
  private Quaternionf orbitRotation;

  /**
   * Instantiates a new Camera. Initializes all helper vectors and matrices.
   */
  public Camera() {
    this.translativZ = new Vector3f();
    this.translativX = new Vector3f();
    this.translativY = new Vector3f();
    this.position = new Vector3f();
    this.viewMatrix = new Matrix4f();
    this.rotation = new Vector2f();
    this.orbitRotation = new Quaternionf();
  }

  /**
   * Rotates the camera by the given amount.
   * <p>
   * Camera stays aligned to the y plane.
   *
   * @param x Amount (rad) of rotation around the X-Axis.
   * @param y Amount (rad) of rotation around the Y-Axis.
   */
  @SuppressWarnings("unused")
  public void rotate(float x, float y) {
    this.rotation.add(x, y);
    this.recalculate();
  }

  /**
   * Returns the position of the camera.
   *
   * @return Position of the camera. (x,y,z)
   */
  public Vector3f getPosition() {
    return this.position;
  }

  /**
   * Returns the view matrix of the camera.
   *
   * @return view matrix of the camera.
   */
  public Matrix4f getViewMatrix() {
    return this.viewMatrix;
  }

  /**
   * Moves the camera by the given amount.
   *
   * @param x Amount of movement along the X-Axis. + is right, - is left.
   * @param y Amount of movement along the Y-Axis. + is up, - is down.
   * @param z Amount of movement along the Z-Axis. + is forward, - is backward.
   */
  @SuppressWarnings("unused")
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

  /**
   * Moves the camera by the given amount in the given direction.
   *
   * @param inc       Amount of movement.
   * @param direction Direction of movement.
   */
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

  /**
   * recalculates the view matrix.
   */
  private void recalculate() {
    this.viewMatrix.identity()
        .rotate(this.orbitRotation)
        .translate(-this.position.x, -this.position.y, -this.position.z);

  }

  /**
   * Sets the absolute position of the camera.
   *
   * @param x Position along the X-Axis.
   * @param y Position along the Y-Axis.
   * @param z Position along the Z-Axis.
   * @return this
   */
  public Camera setPosition(float x, float y, float z) {
    this.position.set(x, y, z);
    this.recalculate();
    return this;
  }

  /**
   * Sets the absolute position of the camera.
   *
   * @param position Position of the camera. (x,y,z)
   * @return this
   */
  public Camera setPosition(Vector3f position) {
    return this.setPosition(position.x, position.y, position.z);
  }

  /**
   * Sets the absolute rotation of the camera.
   *
   * @param x (rad) Rotation around the X-Axis.
   * @param y (rad) Rotation around the Y-Axis.
   * @return this
   */
  public Camera setRotation(float x, float y) {
    this.rotation.set(x, y);
    this.recalculate();
    return this;
  }

  /**
   * Moves the orbit of the camera by the given amount.
   *
   * @param x Amount (rad) of rotation around the X-Axis.
   * @param y Amount (rad) of rotation around the Y-Axis.
   * @param z Amount (rad) of rotation around the Z-Axis.
   */
  @SuppressWarnings("UnusedReturnValue")
  public Camera moveOrbit(float x, float y, float z) {
    y = (float) ((double) y * 2.0 * Math.PI);
    x = (float) ((double) x * 2.0 * Math.PI);
    z = (float) ((double) z * 2.0 * Math.PI);
    this.orbitRotation.rotateYXZ(y, x, z);
    //this.viewMatrix.rotate(this.orbitRotation);
    this.recalculate();
    return this;
  }

  /**
   * Sets the absolute orbit of the camera.
   *
   * @param x (rad) Rotation around the X-Axis.
   * @param y (rad) Rotation around the Y-Axis.
   * @param z (rad) Rotation around the Z-Axis.
   * @return this
   */
  @SuppressWarnings("UnusedReturnValue")
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

  /**
   * Sets the position and the point the camera is looking at.
   *
   * @param cameraPosition Position of the camera. (x,y,z)
   * @param objectPosition Position of the point the camera is looking at.
   * @return this
   */
  @SuppressWarnings("UnusedReturnValue")
  public Camera setPositionAndOrientation(Vector3f cameraPosition, Vector3f objectPosition) {
    var lookDir = new Vector3f(objectPosition).sub(cameraPosition).normalize();
    var yNorm = new Vector3f(0, 1, 0).normalize();
    var right = new Vector3f(lookDir).cross(yNorm).normalize();
    if (Float.isNaN(right.x()) || Float.isNaN(right.y()) || Float.isNaN(right.y())) {
      right = new Vector3f(1, 0, 0);
    }
    var up = new Vector3f(right).cross(lookDir).normalize();
    this.position.set(cameraPosition);
    this.orbitRotation = new Quaternionf();
    this.orbitRotation.lookAlong(lookDir, up);
    this.recalculate();
    return this;
  }

  /**
   * Sets the position and the point the camera is looking at. Furthermore, it sets the up vector of the camera.
   *
   * @param cameraPosition Position of the camera.
   * @param objectPosition Position of the point the camera is looking at.
   * @param up             Up vector of the camera.
   * @return this
   */
  @SuppressWarnings("UnusedReturnValue")
  public Camera setPositionAndOrientation(Vector3f cameraPosition, Vector3f objectPosition, Vector3f up) {
    var lookDir = new Vector3f(objectPosition).sub(cameraPosition).normalize();

    this.setPosition(cameraPosition);
    this.orbitRotation.lookAlong(lookDir, up);
    this.recalculate();
    return this;
  }


  /**
   * Helper method to handle the degrees over 360 and under 0.
   */
  @SuppressWarnings("unused")
  public float degreeHandler(float degree) {
    if (degree > 360) {
      degree -= 360;
    } else if (degree < 0) {
      degree += 360;
    }
    return degree;
  }

}
