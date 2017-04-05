package org.vitrivr.cineast.core.config;

import java.util.UUID;

import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;

public class QueryConfig extends ReadableQueryConfig {

  public QueryConfig(ReadableQueryConfig qc) {
    super(qc);
  }

  private QueryConfig(ReadableQueryConfig qc, UUID uuid) {
    super(qc, uuid);
  }

  public QueryConfig setDistanceWeights(float[] weights) {
    this.distanceWeights = weights;
    return this;
  }

  public QueryConfig setDistanceIfEmpty(Distance distance) {
    if (this.distance == null) {
      return setDistance(distance);
    }
    return this;
  }

  public QueryConfig setDistanceWeightsIfEmpty(float[] weights) {
    if (this.distanceWeights == null) {
      return setDistanceWeights(weights);
    }
    return this;
  }

  public QueryConfig setNormIfEmty(float norm) {
    if (Float.isNaN(this.norm)) {
      return setNorm(norm);
    }
    return this;
  }

  public QueryConfig clone() {
    return new QueryConfig(this);
  }

  public QueryConfig cloneWithNewQueryId() {
    return new QueryConfig(this, UUID.randomUUID());
  }

  public static QueryConfig clone(ReadableQueryConfig qc) {
    return new QueryConfig(qc);
  }

  public static QueryConfig notNull(QueryConfig qc) {
    if (qc == null) {
      return new QueryConfig(null);
    }
    return qc;
  }

  /**
   * creates a new {@link QueryConfig} which is identical to the provided one except for the query
   * id
   */
  public static QueryConfig newQueryConfigFromOther(QueryConfig qc) {
    return new QueryConfig(qc, null);
  }

//  protected void setNet(NeuralNet net) {
//    this.net = net;
//  }

  protected QueryConfig setDistance(Distance distance) {
    this.distance = distance;
    if (distance == Distance.euclidean) {
      this.norm = 2f;
    } else if (distance == Distance.manhattan) {
      this.norm = 1f;
    } else if (distance == Distance.chebyshev) {
      this.norm = Float.POSITIVE_INFINITY;
    }
    return this;
  }

  protected QueryConfig setNorm(float norm) {
    this.norm = norm;
    if (Math.abs(norm - 2f) < 1e6f) {
      this.distance = Distance.euclidean;
    } else if (Math.abs(norm - 1f) < 1e6f) {
      this.distance = Distance.manhattan;
    } else if (Float.isInfinite(norm) && norm > 0) {
      this.distance = Distance.chebyshev;
    }
    return this;
  }

//  protected void setClassificationCutoff(float classificationCutoff) {
//    this.classificationCutoff = classificationCutoff;
//  }
}
