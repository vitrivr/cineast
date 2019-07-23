package org.vitrivr.cineast.core.util.distance;

public class MinkowskiDistance implements FloatArrayDistance {

  private static final long serialVersionUID = 1415943182235288096L;
  protected final double exponenet;

  public MinkowskiDistance(double exponent) {
    if (Double.isNaN(exponent)) {
      throw new IllegalArgumentException("exponenet for MinkowskiDistance can not be NaN");
    }
    if (exponent <= 0d) {
      throw new IllegalArgumentException("exponenet for MinkowskiDistance must be positive");
    }
    if (Double.isInfinite(exponent)) {
      throw new IllegalArgumentException("exponenet for MinkowskiDistance must be finite");
    }
    this.exponenet = exponent;
  }

  @Override
  public double applyAsDouble(float[] t, float[] u) {
    if (t == null || u == null) {
      return Double.NaN;
    }

    if (t == u) {
      return 0d;
    }

    int len = Math.min(t.length, u.length);

    double dist = 0d;

    for (int i = 0; i < len; ++i) {
      dist += Math.pow(Math.abs(t[i] - u[i]), exponenet);
    }

    return Math.pow(dist, 1d / exponenet);
  }

}
