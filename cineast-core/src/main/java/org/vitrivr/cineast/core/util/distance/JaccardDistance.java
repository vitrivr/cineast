package org.vitrivr.cineast.core.util.distance;

public class JaccardDistance implements FloatArrayDistance {

  private static final long serialVersionUID = 8880002586109985666L;

  @Override
  public double applyAsDouble(float[] t, float[] u) {
    if (t == null || u == null) {
      return Double.NaN;
    }

    if (t == u) {
      return 0d;
    }

    int len = Math.min(t.length, u.length);

    double min = 0d, max = 0d;

    for (int i = 0; i < len; ++i) {
      min += Math.min(t[i], u[i]);
      max += Math.max(t[i], u[i]);
    }

    return 1d - (min / max);
  }

}
