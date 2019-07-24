package org.vitrivr.cineast.core.util.distance;

public class ManhattanDistance implements FloatArrayDistance {

  private static final long serialVersionUID = -8292041573308715141L;

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
      dist += Math.abs(t[i] - u[i]);
    }

    return dist;
  }

}
