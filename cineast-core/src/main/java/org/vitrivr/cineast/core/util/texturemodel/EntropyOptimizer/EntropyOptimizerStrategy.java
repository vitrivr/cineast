package org.vitrivr.cineast.core.util.texturemodel.EntropyOptimizer;

/**
 * The method used to calculate the entropy.
 */
public enum EntropyOptimizerStrategy {
  /**
   * The new view vector is chosen randomly.
   */
  RANDOMIZED,
  /**
   * The new view vector is chosen by the gradient of the entropy.
   */
  NEIGHBORHOOD,
}
