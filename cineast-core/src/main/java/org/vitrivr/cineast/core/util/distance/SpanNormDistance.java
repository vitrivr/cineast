package org.vitrivr.cineast.core.util.distance;

public class SpanNormDistance implements FloatArrayDistance {

  private static final long serialVersionUID = -5074710841339028967L;

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
      max = Math.max(max, (t[i] - u[i]));
      min = Math.min(min, (t[i] - u[i]));
    }

    return max - min;
  }

}
