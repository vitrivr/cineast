package org.vitrivr.cineast.core.util.distance;

public class WeightedCorrelationDistance extends CorrelationDistance {

  private static final long serialVersionUID = -6136429047726610016L;
  private final float[] weights;

  WeightedCorrelationDistance(float[] weights) {
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

    double ma = 0d, mb = 0d;

    for (int i = 0; i < len; ++i) {
      ma += t[i];
      mb += u[i];
    }

    ma /= len;
    mb /= len;

    double dot = 0d, a = 0d, b = 0d;
    double a_, b_;

    for (int i = 0; i < len; ++i) {
      a_ = (t[i] - ma) * this.weights[i];
      b_ = (u[i] - mb) * this.weights[i];
      dot += a_ * b_;
      a += a_ * a_;
      b += b_ * b_;
    }

    return 1d - (dot / (Math.sqrt(a) * Math.sqrt(b)));
  }

}
