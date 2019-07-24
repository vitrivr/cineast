package org.vitrivr.cineast.core.util.distance;

public class HaversineDistance implements FloatArrayDistance {

  private static final double EARTH_RADIUS = 6371d; // Approx Earth radius in KM

  @Override
  public double applyAsDouble(float[] t, float[] u) {
    double dLat  = Math.toRadians((u[0] - t[0]));
    double dLong = Math.toRadians((u[1] - t[1]));

    double startLat = Math.toRadians(t[0]);
    double endLat   = Math.toRadians(u[0]);

    double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS * c;
  }

  private static double haversin(double val) {
    return Math.pow(Math.sin(val / 2), 2);
  }

}
