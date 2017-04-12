package org.vitrivr.cineast.core.config;

import java.util.Optional;
import java.util.UUID;

import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;

public class ReadableQueryConfig {

  public static enum Distance {
    chisquared, correlation, cosine, hamming, jaccard, kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, minkowski, spannorm, haversine
  }

  protected Distance distance = null;
  protected float[] distanceWeights = null;
  protected float norm = Float.NaN;
  protected CorrespondenceFunction correspondence = null;
//  protected NeuralNet net = null; // TODO remove from here
//  protected float classificationCutoff = Float.NaN; // TODO find better place
  private final UUID queryId;
  
  protected ReadableQueryConfig(ReadableQueryConfig qc, UUID uuid){
    this.queryId = (uuid == null) ? UUID.randomUUID() : uuid;
    if(qc == null){
      return;
    }
    this.distance = qc.distance;
    this.distanceWeights = qc.distanceWeights;
    this.norm = qc.norm;
//    this.net = qc.net;
//    this.classificationCutoff = qc.classificationCutoff;
  }
  
  public ReadableQueryConfig(ReadableQueryConfig qc){
    this(qc, qc == null ? null : qc.queryId);
  }

//  public Optional<Float> getCutoff() {
//    return Optional.ofNullable(Float.isNaN(classificationCutoff) ? null : classificationCutoff);
//  }
  
  public Optional<Distance> getDistance(){
    return Optional.ofNullable(this.distance);
  }
  
  public Optional<Float> getNorm(){
    return Optional.ofNullable(Float.isNaN(norm) ? null : norm);
  }

  public Optional<CorrespondenceFunction> getCorrespondenceFunction() {
    return Optional.ofNullable(this.correspondence);
  }

  public final UUID getQueryId(){
    return this.queryId;
  }
  
//  public Optional<NeuralNet> getNet() {
//    return Optional.ofNullable(this.net);
//  }
  
  public Optional<float[]> getDistanceWeights() {
    return Optional.ofNullable(this.distanceWeights);
  }


}
