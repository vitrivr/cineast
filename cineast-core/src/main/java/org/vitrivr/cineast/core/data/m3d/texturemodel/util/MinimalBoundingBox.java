package org.vitrivr.cineast.core.data.m3d.texturemodel.util;

import io.grpc.SynchronizationContext.ScheduledHandle;
import java.util.LinkedList;
import java.util.List;
import org.joml.Vector3f;

public class MinimalBoundingBox {

  public static final float MAX = Float.MAX_VALUE;
  public static final float MIN = -1f * Float.MAX_VALUE;
  private final Vector3f vMax = new Vector3f(MIN, MIN, MIN);
  private final Vector3f vMin = new Vector3f(MAX, MAX, MAX);

  private final Vector3f com = new Vector3f(0f, 0f, 0f);

  private final float norm = 1f;

  private float scalingFactorToNorm = 1f;

  private final Vector3f translationToNorm = new Vector3f(0, 0, 0);

  public MinimalBoundingBox() {
  }

  public MinimalBoundingBox(float[] positions) {
    this.update(positions);
  }

  public MinimalBoundingBox(List<Vector3f> positions) {
    this.update(positions);
  }

  public List<Vector3f> toList() {
    var vec = new LinkedList<Vector3f>();
    if (this.isValidBoundingBox()) {
      vec.add(vMax);
      vec.add(vMin);
    }
    return vec;
  }

  public MinimalBoundingBox merge(MinimalBoundingBox other) {
    if (this.equals(other)) {
      return this;
    }
    this.update(other.toList());
    return this;
  }

  public float getScalingFactorToNorm() {
    return this.scalingFactorToNorm;
  }

  public Vector3f getTranslationToNorm() {
    return this.translationToNorm;
  }

  protected void update(float[] positions) {
    var vectors = new LinkedList<Vector3f>();
    for (var ic = 0; ic < positions.length; ic += 3) {
      vectors.add(new Vector3f(positions[ic], positions[ic + 1], positions[ic + 2]));
    }
    this.update(vectors);
  }

  protected void update(List<Vector3f> vec) {
    // Has to be exact this sequence
    if (this.updateBounds(vec)) {
      this.updateCom();
      this.updateScalingFactorToNorm();
      this.updateTranslationToNorm();
    }
  }

  private boolean isValidBoundingBox(){
    return this.vMax.x > this.vMin.x && this.vMax.y > this.vMin.y && this.vMax.z > this.vMin.z;
  }

  private void updateCom() {
    this.com.set(new Vector3f((this.vMax.x + this.vMin.x) / 2f, (this.vMax.y + this.vMin.y) / 2f, (this.vMax.z + this.vMin.z) / 2f));
  }

  private void updateTranslationToNorm() {
    this.translationToNorm.set(new Vector3f(com.mul(this.scalingFactorToNorm)));
  }

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

  private boolean updateBounds(List<Vector3f> positions) {
    var changed = false;
    for (var vec : positions) {
      changed |= this.updateBounds(vec);
    }
    return changed;
  }

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
}
