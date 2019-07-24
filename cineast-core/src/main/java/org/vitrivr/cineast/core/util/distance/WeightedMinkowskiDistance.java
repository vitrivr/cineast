package org.vitrivr.cineast.core.util.distance;

public class WeightedMinkowskiDistance extends MinkowskiDistance {

  private static final long serialVersionUID = 6196111620066310228L;
  private final float[] weights;

  WeightedMinkowskiDistance(float[] weights, double exponent) {
    super(exponent);
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
      dist += Math.pow(t[i] - u[i], exponenet) * this.weights[i];
    }

    return Math.pow(dist, 1d / exponenet);
  }
  
}
