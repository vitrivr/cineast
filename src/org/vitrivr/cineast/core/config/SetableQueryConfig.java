package org.vitrivr.cineast.core.config;

import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;

public class SetableQueryConfig extends QueryConfig {

  public SetableQueryConfig(ReadableQueryConfig qc) {
    super(qc);
  }

  @Override
  public void setNet(NeuralNet net) {
    super.setNet(net);
  }

  @Override
  public QueryConfig setDistance(Distance distance) {
    return super.setDistance(distance);
  }

  @Override
  public QueryConfig setNorm(float norm) {
    return super.setNorm(norm);
  }

  @Override
  public void setClassificationCutoff(float classificationCutoff) {
    super.setClassificationCutoff(classificationCutoff);
  }

 
  
}
