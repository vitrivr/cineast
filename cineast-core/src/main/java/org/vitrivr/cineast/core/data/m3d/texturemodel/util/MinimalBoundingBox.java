package org.vitrivr.cineast.core.data.m3d.texturemodel.util;

import java.util.LinkedList;
import java.util.List;
import org.joml.Vector3f;

/**
 * This class represents a minimal bounding box. It can be generated from a list of vertices. It can be merged with another minimal bounding box.
 */
public class MinimalBoundingBox {

  /**
   * Constant for the maximum float value.
   */
  public static final float MAX = Float.MAX_VALUE;
  /**
   * Constant for the minimum float value.
   */
  public static final float MIN = -1f * Float.MAX_VALUE;
  /**
   * Initial value for the maximum vector. The maximum vector contain the highest (positive if normalized) values for x, y and z  of the bounding box.
   */
  private final Vector3f vMax = new Vector3f(MIN, MIN, MIN);
  /**
   * Initial value for the minimum vector. The minimum vector contain the lowes (negative if normalized) values for x, y and z  of the bounding box.
   */
  private final Vector3f vMin = new Vector3f(MAX, MAX, MAX);

  /**
   * Center of mass of the bounding box as x, y, z vector.
   */
  private final Vector3f com = new Vector3f(0f, 0f, 0f);

  /**
   * Norm to which the bounding box is scaled. Usually 1.
   */
  private final float norm = 1f;

  /**
   * Scaling factor to norm. The scaling factor is the factor to scale the bounding box to the norm. 1 for no scaling.
   */
  private float scalingFactorToNorm = 1f;

  /**
   * Translation to norm. The translation is the vector to translate com of the bounding box to the origin. (0, 0, 0) for no translation.
   */
  private final Vector3f translationToNorm = new Vector3f(0, 0, 0);

  /**
   * Empty constructor to initialize an empty bounding box The purpose is to iteratively add bounding boxes.
   */
  public MinimalBoundingBox() {
  }

  /**
   * Constructor to initialize a bounding box from an array of vertices. The resulting bounding box is the minimal bounding box that contains all vertices. The bounding box is aligned with the Cartesian coordinate system.
   *
   * @param positions List of vertices.
   */
  public MinimalBoundingBox(float[] positions) {
    this.update(positions);
  }

  /**
   * Constructor to initialize a bounding box from a list of vertices. The resulting bounding box is the minimal bounding box that contains all vertices. The bounding box is aligned with the Cartesian coordinate system.
   *
   * @param positions List of vertices.
   */
  public MinimalBoundingBox(List<Vector3f> positions) {
    this.update(positions);
  }

  /**
   * Crate list of vertices from bounding box. List contains the maximum and minimum vector of the bounding box.
   * TODO: A better approach would be to return the 8 vertices of the bounding box. But needs to be checked before.
   * @return List of vertices.
   */
  public List<Vector3f> toList() {
    var vec = new LinkedList<Vector3f>();
    if (this.isValidBoundingBox()) {
      vec.add(vMax);
      vec.add(vMin);
    }
    return vec;
  }

  /**
   * Merge this bounding box with another bounding box. The resulting bounding box is the minimal bounding box that contains both bounding boxes.
   *
   * @param other Bounding box to merge with.
   * @return Merged bounding box.
   */
  public MinimalBoundingBox merge(MinimalBoundingBox other) {
    if (this.equals(other)) {
      return this;
    }
    this.update(other.toList());
    return this;
  }

  /**
   * Returns the scaling factor to norm size.
   *
   * @return Scaling factor to norm size.
   */
  public float getScalingFactorToNorm() {
    return this.scalingFactorToNorm;
  }

  /**
   * Get translation to Origin.
   *
   * @return Translation to Origin.
   */
  public Vector3f getTranslationToNorm() {
    return this.translationToNorm;
  }

  /**
   * Helper method to add data to the bounding box and recalculate the bounding boxes values.
   */
  protected void update(float[] positions) {
    var vectors = new LinkedList<Vector3f>();
    for (var ic = 0; ic < positions.length; ic += 3) {
      vectors.add(new Vector3f(positions[ic], positions[ic + 1], positions[ic + 2]));
    }
    this.update(vectors);
  }

  /**
   * Helper method to add data to the bounding box and recalculate the bounding boxes values.
   * Since the calculation of the bounding box is iterative, the calculation is split into several steps.
   * The steps are:
   * <ul>
   * <li>1. Update the center of mass.</li>
   * <li>2. Update the scaling factor to norm.</li>
   * <li>3. Update the translation to norm.</li>
   * </ul>
   *  These steps had to be exact in this sequence
   */
  protected void update(List<Vector3f> vec) {
    // Has to be exact this sequence
    if (this.updateBounds(vec)) {
      this.updateCom();
      this.updateScalingFactorToNorm();
      this.updateTranslationToNorm();
    }
  }

  /**
   * Checks if the bounding box is valid.
   * A bounding box is valid if each component of the maximum vector is greater than the corresponding component of the minimum vector.
   * @return True if the bounding box is valid, false otherwise.
   */
  private boolean isValidBoundingBox() {
    return this.vMax.x > this.vMin.x && this.vMax.y > this.vMin.y && this.vMax.z > this.vMin.z;
  }

  /**
   * Update the center of mass.
   * The center of mass is the middle point of the bounding box.
   */
  private void updateCom() {
    this.com.set(new Vector3f((this.vMax.x + this.vMin.x) / 2f, (this.vMax.y + this.vMin.y) / 2f, (this.vMax.z + this.vMin.z) / 2f));
  }

  /**
   * Update the translation to norm.
   * The translation is the vector to translate com of the bounding box to the origin. (0, 0, 0) for no translation.
   */
  private void updateTranslationToNorm() {
    this.translationToNorm.set(new Vector3f(com.mul(this.scalingFactorToNorm)));
  }

  /**
   * Update the scaling factor to norm.
   * The scaling factor is the factor to scale longest vector in the bounding box to the norm. 1 for no scaling.
   */
  private void updateScalingFactorToNorm() {
    var farthest = new Vector3f(0, 0, 0);
    for (var vec : this.toList()) {
      var vector = new Vector3f(vec).sub(this.com);
      if (vector.length() > farthest.length()) {
        farthest = vector;
      }
      this.scalingFactorToNorm = 1f / (farthest.length() * 2);
    }
  }

  /**
   * Update the bounding box with a new vectors
   * @return True if the bounding box has changed, false otherwise.
   */
  private boolean updateBounds(List<Vector3f> positions) {
    var changed = false;
    for (var vec : positions) {
      changed |= this.updateBounds(vec);
    }
    return changed;
  }

  /**
   * Update the bounding box with a new vector
   * @return True if the bounding box has changed, false otherwise.
   */
  private boolean updateBounds(Vector3f vec) {
    var changed = false;
    if (vec.x > this.vMax.x) {
      this.vMax.x = vec.x;
      changed = true;
    }
    if (vec.x < this.vMin.x) {
      this.vMin.x = vec.x;
      changed = true;
    }
    if (vec.y > this.vMax.y) {
      this.vMax.y = vec.y;
      changed = true;
    }
    if (vec.y < this.vMin.y) {
      this.vMin.y = vec.y;
      changed = true;
    }
    if (vec.z > this.vMax.z) {
      this.vMax.z = vec.z;
      changed = true;
    }
    if (vec.z < this.vMin.z) {
      this.vMin.z = vec.z;
      changed = true;
    }
    return changed;
  }

  /**
   * Release all resources.
   */
  public void close() {

  }
}
