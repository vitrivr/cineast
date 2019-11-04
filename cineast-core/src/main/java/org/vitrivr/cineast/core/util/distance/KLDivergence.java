package org.vitrivr.cineast.core.util.distance;

public class KLDivergence implements FloatArrayDistance {

  private static final long serialVersionUID = 2390880385355432436L;

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
      if (Math.abs(u[i]) < 1e-6 || t[i] < 0 || t[i] / u[i] <= 0) {
        continue;
      }
      dist += t[i] * Math.log(t[i] / u[i]);

    }

    return dist;
  }

}
