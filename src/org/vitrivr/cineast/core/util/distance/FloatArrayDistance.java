package org.vitrivr.cineast.core.util.distance;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;

public interface FloatArrayDistance extends Distance<float[]>{

  /**
   * returns the distance between the two provided arrays or NaN if at least one of them is <code>null</code>.
   */
  double applyAsDouble(float[] t, float[] u);

  public static FloatArrayDistance fromDistance(final ReadableQueryConfig.Distance distance){//TODO add other distances
    if(distance == null){
      throw new NullPointerException("distance cannot be null");
    }
    if(distance == ReadableQueryConfig.Distance.minkowski){
      throw new IllegalArgumentException("cannot instanciate minkowski distance without a norm");
    }
    
    switch(distance){
    case chebyshev: return new ChebyshevDistance();
    case chisquared: return new ChiSquaredDistance();
    case correlation: return new CorrelationDistance();
    case cosine: return new CosineDistance();
    case euclidean: return new EuclideanDistance();
    case hamming: return new HammingDistance();
    case haversine:
      break;
    case jaccard: return new JaccardDistance();
    case kullbackleibler: return new KLDivergence();
    case manhattan: return new ManhattanDistance();
    case spannorm: return new SpanNormDistance();
    case squaredeuclidean: return new SquaredEuclideanDistance();
    default:
      break;
    
    }
    
    throw new IllegalStateException("distance not yet implemented!");
    
  }
  
  public static FloatArrayDistance fromQueryConfig(ReadableQueryConfig queryConfig){
    if(queryConfig == null){
      throw new NullPointerException("queryConfig cannot be null");
    }
    
    if(!queryConfig.getDistance().isPresent()){
      return null;
    }
    
    ReadableQueryConfig.Distance distance = queryConfig.getDistance().get();
    
    if(distance == ReadableQueryConfig.Distance.minkowski){
      if(!queryConfig.getNorm().isPresent()){
        throw new IllegalArgumentException("cannot instanciate minkowski distance without a norm");
      }
      return new MinkowskiDistance(queryConfig.getNorm().get());
    }
    
    return fromDistance(distance);
    
  }
  
}
