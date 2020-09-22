package org.vitrivr.cineast.core.util.distance;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

public interface FloatArrayDistance extends Distance<float[]>, DistanceMeasure {

  static final long serialVersionUID = 1L;
  
  /**
   * returns the distance between the two provided arrays or NaN if at least one of them is
   * <code>null</code>.
   */
  @Override
  double applyAsDouble(float[] t, float[] u);

  
  
  @Override
  default double compute(double[] a, double[] b) throws DimensionMismatchException {
    if(a == null || b == null){
      throw new NullPointerException();
    }
    if(a.length != b.length){
      throw new DimensionMismatchException(a.length, b.length);
    }
    float[] fa = new float[a.length], fb = new float[b.length];
    for(int i = 0; i < a.length; ++i){
      fa[i] = (float) a[i];
      fb[i] = (float) b[i];
    }
    return applyAsDouble(fa, fb);
  }



  public static FloatArrayDistance fromDistance(final ReadableQueryConfig.Distance distance) {
    if (distance == null) {
      throw new NullPointerException("distance cannot be null");
    }
    if (distance == ReadableQueryConfig.Distance.minkowski) {
      throw new IllegalArgumentException("cannot instanciate minkowski distance without a norm");
    }

    switch (distance) {
    case chebyshev:
      return new ChebyshevDistance();
    case chisquared:
      return new ChiSquaredDistance();
    case correlation:
      return new CorrelationDistance();
    case cosine:
      return new CosineDistance();
    case euclidean:
      return new EuclideanDistance();
    case hamming:
      return new HammingDistance();
    case haversine:
      return new HaversineDistance();
    case jaccard:
      return new JaccardDistance();
    case kullbackleibler:
      return new KLDivergence();
    case manhattan:
      return new ManhattanDistance();
    case spannorm:
      return new SpanNormDistance();
    case squaredeuclidean:
      return new SquaredEuclideanDistance();
    default:
      break;

    }

    throw new IllegalStateException("distance not implemented!");

  }

  public static FloatArrayDistance fromQueryConfig(ReadableQueryConfig queryConfig) {
    if (queryConfig == null) {
      throw new NullPointerException("queryConfig cannot be null");
    }

    if (!queryConfig.getDistance().isPresent()) {
      return new ManhattanDistance();
    }

    ReadableQueryConfig.Distance distance = queryConfig.getDistance().get();

    if (distance == ReadableQueryConfig.Distance.minkowski) {
      if (!queryConfig.getNorm().isPresent()) {
        throw new IllegalArgumentException("cannot instanciate minkowski distance without a norm");
      }
      if (queryConfig.getDistanceWeights().isPresent()) {
        return new WeightedMinkowskiDistance(queryConfig.getDistanceWeights().get(),
            queryConfig.getNorm().get());
      }
      return new MinkowskiDistance(queryConfig.getNorm().get());
    }

    if (queryConfig.getDistanceWeights().isPresent()) {
      float[] weights = queryConfig.getDistanceWeights().get();
      switch (distance) {
      case chebyshev:
        return new WeightedChebyshevDistance(weights);
      case chisquared:
        return new WeightedChiSquaredDistance(weights);
      case correlation:
        return new WeightedCorrelationDistance(weights);
      case cosine:
        return new WeightedCosineDistance(weights);
      case euclidean:
        return new WeightedEuclideanDistance(weights);
      case hamming:
        return new WeightedHammingDistance(weights);
      case jaccard:
        return new WeightedJaccardDistance(weights);
      case kullbackleibler:
        return new WeightedKLDivergence(weights);
      case manhattan:
        return new WeightedManhattanDistance(weights);
      case spannorm:
        return new WeightedSpanNormDistance(weights);
      case squaredeuclidean:
        return new WeightedSquaredEuclideanDistance(weights);
      default:
        throw new IllegalStateException("weighted distance not implemented!");

      }
    }

    return fromDistance(distance);

  }

}
