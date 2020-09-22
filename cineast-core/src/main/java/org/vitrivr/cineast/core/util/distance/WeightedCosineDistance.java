package org.vitrivr.cineast.core.util.distance;

public class WeightedCosineDistance extends CosineDistance {

  private static final long serialVersionUID = 1908564141547874227L;
  private final float[] weights;

  WeightedCosineDistance(float[] weights) {
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

    double dot = 0d, a = 0d, b = 0d;

    for (int i = 0; i < len; ++i) {
      dot += t[i] * u[i] * this.weights[i];
      a += t[i] * t[i] * this.weights[i];
      b += u[i] * u[i] * this.weights[i];
    }

    return 1d - (dot / (Math.sqrt(a) * Math.sqrt(b)));
  }
}
