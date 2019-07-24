package org.vitrivr.cineast.core.util.distance;

public class WeightedSpanNormDistance extends SpanNormDistance {

  private static final long serialVersionUID = -2854063634272230480L;
  private final float[] weights;

  WeightedSpanNormDistance(float[] weights) {
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

    double min = 0d, max = 0d;

    for (int i = 0; i < len; ++i) {
      max = Math.max(max, (t[i] - u[i]) * this.weights[i]);
      min = Math.min(min, (t[i] - u[i]) * this.weights[i]);
    }

    return max - min;
  }
  
}
