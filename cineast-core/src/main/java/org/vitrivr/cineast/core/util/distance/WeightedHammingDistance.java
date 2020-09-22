package org.vitrivr.cineast.core.util.distance;

public class WeightedHammingDistance extends HammingDistance {

  private static final long serialVersionUID = -3349581065511403145L;
  private final float[] weights;

  WeightedHammingDistance(float[] weights) {
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
      dist += Float.compare(t[i], u[i]) == 0 ? 0d : this.weights[i];
    }

    return dist;
  }

}
