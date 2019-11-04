package org.vitrivr.cineast.core.data;

import com.google.common.base.Preconditions;

import java.util.function.DoubleUnaryOperator;

public class CorrespondenceFunction implements DoubleUnaryOperator {
  private static final CorrespondenceFunction IDENTITY = new CorrespondenceFunction(DoubleUnaryOperator.identity());

  private final DoubleUnaryOperator function;

  private CorrespondenceFunction(DoubleUnaryOperator function) {
    this.function = function;
  }

  @Override
  public final double applyAsDouble(double distance) {
    double score = function.applyAsDouble(distance);
    if (Double.isNaN(score)) {
      return 0d;
    }
    if (score > 1d) {
      return 1d;
    }
    if (score < 0d) {
      return 0d;
    }
    return score;
  }

  public static CorrespondenceFunction fromFunction(DoubleUnaryOperator function) {
    return new CorrespondenceFunction(function);
  }

  public static CorrespondenceFunction identity() {
    return IDENTITY;
  }

  public static CorrespondenceFunction linear(double maximumDistance) {
    Preconditions.checkArgument(maximumDistance > 0, "Maximum distance cannot be zero or smaller than zero, but found: " + maximumDistance);
    return new CorrespondenceFunction(distance -> 1d - (distance / maximumDistance));
  }

  public static CorrespondenceFunction hyperbolic(double divisor) {
    Preconditions.checkArgument(divisor > 0, "Hyperbolic divisor cannot be zero or smaller than zero, but found: " + divisor);
    return new CorrespondenceFunction(distance -> 1d / (1d + distance / divisor));
  }
}
