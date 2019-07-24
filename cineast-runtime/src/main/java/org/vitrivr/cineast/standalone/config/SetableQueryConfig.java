package org.vitrivr.cineast.standalone.config;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

public class SetableQueryConfig extends QueryConfig {

  public SetableQueryConfig(ReadableQueryConfig qc) {
    super(qc);
  }

//  @Override
//  public void setNet(NeuralNet net) {
//    super.setNet(net);
//  }

  @Override
  public QueryConfig setDistance(Distance distance) {
    return super.setDistance(distance);
  }

  @Override
  public QueryConfig setNorm(float norm) {
    return super.setNorm(norm);
  }

//  @Override
//  public void setClassificationCutoff(float classificationCutoff) {
//    super.setClassificationCutoff(classificationCutoff);
//  }

 
  
}
