package org.vitrivr.cineast.core.util.distance;

public class WeightedEuclideanDistance extends EuclideanDistance {

  private static final long serialVersionUID = -3515846552848309853L;
  private final float[] weights;

  WeightedEuclideanDistance(float[] weights) {
    if (weights == null) {
      throw new NullPointerException("weights cannot be null");
    }
    if (weights.length == 0) {
      throw new IllegalArgumentException("weights cannot be empty");
    }
    this.weights = weights.clone();
  }

  @Override
  public double applyAsDouble(float[] t, float[] u) {
    if (t == null || u == null) {
      return Double.NaN;
    }

    if (t == u) {
      return 0d;
    }

    int len = Math.min(Math.min(t.length, u.length), this.weights.length);

    double dist = 0d;

    for (int i = 0; i < len; ++i) {
      dist += (t[i] - u[i]) * (t[i] - u[i]) * this.weights[i];
    }

    return Math.sqrt(dist);
  }

}
