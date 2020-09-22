package org.vitrivr.cineast.core.util.distance;

public class WeightedChebyshevDistance extends ChebyshevDistance {

  private static final long serialVersionUID = 3146437065362382786L;
  private final float[] weights;

  WeightedChebyshevDistance(float[] weights) {
    if (weights == null) {
      throw new NullPointerException("weights cannot be null");
    }
    if (weights.length == 0) {
      throw new IllegalArgumentException("weights cannot be empty");
    }
    this.weights = weights.clone();
  }
  
  @Override
  public double applyAsDouble(float[] t, float[] u) {
    if(t == null || u == null){
      return Double.NaN;
    }
    
    if(t == u){
      return 0d;
    }
    
    int len = Math.min(Math.min(t.length, u.length), this.weights.length);
    
    double dist = 0d;
    
    for(int i = 0; i < len; ++i){
      dist = Math.max(dist, Math.abs(t[i] - u[i]) * this.weights[i]);
    }
    
    return dist;
  }
  
}
