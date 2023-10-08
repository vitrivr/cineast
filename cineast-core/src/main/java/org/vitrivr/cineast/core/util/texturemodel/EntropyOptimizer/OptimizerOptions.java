package org.vitrivr.cineast.core.util.texturemodel.EntropyOptimizer;

import org.joml.Vector3f;

/**
 * Options for the ModelEntropyOptimizer and entropy calculation.
 */
public class OptimizerOptions {
  /**
   * The factor the unit vector is multiplied with to zoom.
   * <p>
   *   > 1 zooms out, < 1 zooms in.
   */
  public float zoomOutFactor = 1f; //(float) Math.sqrt(3.0);
  /**
   * The method used to optimize the entropy.
   */
  public EntropyOptimizerStrategy optimizer = EntropyOptimizerStrategy.RANDOMIZED;
  /**
   * The method used to calculate the entropy.
   */
  public EntopyCalculationMethod method = EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA;
  /**
   * The maximum number of iterations the optimizer should perform.
   */
  public int iterations = 1000;
  /**
   * The initial view vector.
   */
  public Vector3f initialViewVector = new Vector3f(0f, 0f, 1f);
  /**
   * Weight for y normal vectors pointing up.
   */
  public float yPosWeight = 1;
  /**
   * Weight for y normal vectors pointing down.
   */
  public float yNegWeight = 1;
}
