package org.vitrivr.cineast.core.util.distance;

public class KLDivergence implements FloatArrayDistance {

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
      dist += t[i] * Math.log(t[i] / u[i]);
    }

    return dist;
  }

}
