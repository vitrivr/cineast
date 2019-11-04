package org.vitrivr.cineast.core.util.distance;

public class EuclideanDistance implements FloatArrayDistance {

  private static final long serialVersionUID = -8415611991170039363L;

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
      dist += (t[i] - u[i]) * (t[i] - u[i]);
    }

    return Math.sqrt(dist);
  }

}
