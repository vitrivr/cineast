package org.vitrivr.cineast.core.util.distance;

public class ChiSquaredDistance implements FloatArrayDistance {

  private static final long serialVersionUID = -7710769801163419590L;

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
      if (Math.abs(t[i] + u[i]) < 1e-6) {
        continue;
      }
      dist += ((t[i] - u[i]) * (t[i] - u[i])) / (t[i] + u[i]);
    }

    return dist;
  }

}
