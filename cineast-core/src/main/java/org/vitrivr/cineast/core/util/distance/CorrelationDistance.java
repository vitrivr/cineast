package org.vitrivr.cineast.core.util.distance;

public class CorrelationDistance implements FloatArrayDistance {

  private static final long serialVersionUID = 871539863056627306L;

  @Override
  public double applyAsDouble(float[] t, float[] u) {
    if (t == null || u == null) {
      return Double.NaN;
    }

    if (t == u) {
      return 0d;
    }

    int len = Math.min(t.length, u.length);

    double ma = 0d, mb = 0d;

    for (int i = 0; i < len; ++i) {
      ma += t[i];
      mb += u[i];
    }

    ma /= len;
    mb /= len;

    double dot = 0d, a = 0d, b = 0d;
    double a_, b_;

    for (int i = 0; i < len; ++i) {
      a_ = t[i] - ma;
      b_ = u[i] - mb;
      dot += a_ * b_;
      a += a_ * a_;
      b += b_ * b_;
    }

    double div = (Math.sqrt(a) * Math.sqrt(b));

    if (div < 1e-6 || Double.isNaN(div)) {
      return 1d;
    }

    return 1d - (dot / div);
  }

}
