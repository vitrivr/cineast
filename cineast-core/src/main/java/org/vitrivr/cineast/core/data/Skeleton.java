package org.vitrivr.cineast.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import georegression.struct.point.Point2D_F32;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Skeleton {

  public static final SkeletonPointName[][] BONES = {
      {SkeletonPointName.LEFT_ANKLE, SkeletonPointName.LEFT_KNEE},
      {SkeletonPointName.LEFT_KNEE, SkeletonPointName.LEFT_HIP},
      {SkeletonPointName.RIGHT_ANKLE, SkeletonPointName.RIGHT_KNEE},
      {SkeletonPointName.RIGHT_KNEE, SkeletonPointName.RIGHT_HIP},
      {SkeletonPointName.LEFT_HIP, SkeletonPointName.RIGHT_HIP},
      {SkeletonPointName.LEFT_SHOULDER, SkeletonPointName.LEFT_HIP},
      {SkeletonPointName.RIGHT_SHOULDER, SkeletonPointName.RIGHT_HIP},
      {SkeletonPointName.LEFT_SHOULDER, SkeletonPointName.RIGHT_SHOULDER},
      {SkeletonPointName.LEFT_SHOULDER, SkeletonPointName.LEFT_ELBOW},
      {SkeletonPointName.RIGHT_SHOULDER, SkeletonPointName.RIGHT_ELBOW},
      {SkeletonPointName.LEFT_ELBOW, SkeletonPointName.LEFT_WRIST},
      {SkeletonPointName.RIGHT_ELBOW, SkeletonPointName.RIGHT_WRIST},
      {SkeletonPointName.LEFT_EYE, SkeletonPointName.RIGHT_EYE},
      {SkeletonPointName.NOSE, SkeletonPointName.LEFT_EYE},
      {SkeletonPointName.NOSE, SkeletonPointName.RIGHT_EYE},
      {SkeletonPointName.LEFT_EYE, SkeletonPointName.LEFT_EAR},
      {SkeletonPointName.RIGHT_EYE, SkeletonPointName.RIGHT_EAR},
      {SkeletonPointName.NOSE, SkeletonPointName.LEFT_SHOULDER},
      {SkeletonPointName.NOSE, SkeletonPointName.RIGHT_SHOULDER}
  };
  private static final int POINT_COUNT = 17;
  private final Point2D_F32[] points = new Point2D_F32[POINT_COUNT];
  private final float[] weights = new float[POINT_COUNT];
  @JsonCreator
  public Skeleton(
      @JsonProperty("coordinates") float[] coordinates,
      @JsonProperty("weights") float[] weights
  ) {

    if (coordinates == null || coordinates.length < 2 * POINT_COUNT || weights == null || weights.length < POINT_COUNT) {
      throw new IllegalArgumentException();
    }

    for (int i = 0; i < POINT_COUNT; ++i) {
      this.weights[i] = weights[i];
      this.points[i] = new Point2D_F32(coordinates[2 * i], coordinates[2 * i + 1]);
    }

  }

  public Skeleton() {
    //empty skeleton
    for (int i = 0; i < POINT_COUNT; ++i) {
      this.points[i] = new Point2D_F32(0f, 0f);
    }
  }

  public List<Point2D_F32> getPoints() {
    ArrayList<Point2D_F32> _return = new ArrayList<>(POINT_COUNT);
    for (int i = 0; i < POINT_COUNT; ++i) {
      _return.add(this.points[i].copy());
    }
    return _return;
  }

  public List<Point2D_F32> getPointsScaled(float scaleX, float scaleY) {
    ArrayList<Point2D_F32> _return = new ArrayList<>(POINT_COUNT);
    for (int i = 0; i < POINT_COUNT; ++i) {
      _return.add(new Point2D_F32(points[i].x * scaleX, points[i].y * scaleY));
    }
    return _return;
  }

  public Point2D_F32 getPoint(SkeletonPointName name) {
    return this.points[name.ordinal()].copy();
  }

  public float getWeight(SkeletonPointName name) {
    return this.weights[name.ordinal()];
  }

  public void setPointWeighted(SkeletonPointName pointName, float weight, Point2D_F32 point) {
    this.weights[pointName.ordinal()] = weight;
    this.points[pointName.ordinal()] = point;
  }

  public enum SkeletonPointName {
    NOSE,
    LEFT_EYE,
    RIGHT_EYE,
    LEFT_EAR,
    RIGHT_EAR,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Skeleton skeleton = (Skeleton) o;
    return Arrays.equals(points, skeleton.points) && Arrays.equals(weights, skeleton.weights);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(points);
    result = 31 * result + Arrays.hashCode(weights);
    return result;
  }
}
