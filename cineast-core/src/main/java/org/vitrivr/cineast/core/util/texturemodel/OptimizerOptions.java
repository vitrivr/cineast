package org.vitrivr.cineast.core.util.texturemodel;

import org.joml.Vector3f;

/**
 * Options for the ModelEntropyOptimizer and entropy calculation.
 */
public class OptimizerOptions {
  public float zoomOutFactor = 1f; //(float) Math.sqrt(3.0);
  public EntropyOptimizerStrategy optimizer = EntropyOptimizerStrategy.RANDOMIZED;
  public EntopyCalculationMethod method = EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA;
  public int iterations = 1000;
  public Vector3f initialViewVector = new Vector3f(0f, 0f, 1f);

}
