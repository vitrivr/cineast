package org.vitrivr.cineast.core.util.distance;

public class HammingDistance implements FloatArrayDistance {

  private static final long serialVersionUID = -1843273404020103987L;

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
      dist += Float.compare(t[i], u[i]) == 0 ? 0d : 1d;
    }

    return dist;
  }

}
