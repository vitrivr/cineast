package org.vitrivr.cineast.core.util.texturemodel.EntropyOptimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;


/**
 * Static class for optimizing the view vector of a model to maximize the viewpoint entropy on the model.
 */
public final class ModelEntropyOptimizer {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Calculates the view vector with the maximum entropy of the model. Uses standard Options for optimizer and entropy calculation.
   *
   * @param model Model to calculate the view vector for.
   * @return View vector with the maximum entropy.
   */
  public static Vector3f getViewVectorWithMaximizedEntropy(IModel model) {
    return getViewVectorWithMaximizedEntropy(model, new OptimizerOptions());
  }

  /**
   * Calculates the view vector with the maximum entropy of the model.
   *
   * @param model   Model to calculate the view vector for.
   * @param options Options for the optimizer and entropy calculation.
   * @return View vector with the maximum entropy.
   */
  public static Vector3f getViewVectorWithMaximizedEntropy(IModel model, OptimizerOptions options) {
    var normals = model.getAllNormals();
    var viewVector = options.initialViewVector;
    var maxEntropyViewVector = optimize(options, normals, viewVector);
    return maxEntropyViewVector;
  }

  /**
   * Wrapper for the optimizer strategy. Optimizes the view vector for the given model with the chosen EntropyOptimizer Strategy.
   *
   * @param options    Options for the optimizer and entropy calculation.
   * @param normals    List of normals of the model.
   * @param viewVector Initial view vector.
   * @return Optimized view vector.
   */
  private static Vector3f optimize(OptimizerOptions options, List<Vector3f> normals, Vector3f viewVector) {
    var optimizer = options.optimizer;
    switch (optimizer) {
      case RANDOMIZED -> {
        return ModelEntropyOptimizer.optimizeRandomized(options, normals, viewVector);
      }
      case NEIGHBORHOOD -> {
        return ModelEntropyOptimizer.optimizeNeighborhood(options, normals, viewVector);
      }
      default -> {
        return new Vector3f(0f, 0f, 1f);
      }
    }
  }

  /**
   * Optimizes the view vector for the given model with the randomized EntropyOptimizer Strategy.
   *
   * @param options    Options for the optimizer and entropy calculation.
   *                   <ul>
   *                     <li> Uses the option iterations. For each iteration a random view vector is generated.</li>
   *                     <li> Uses the option zoomOutFactor. The view vector is zoomed out by this factor.</li>
   *                   </ul>
   * @param normals    List of normals of the model.
   * @param viewVector Initial view vector.
   * @return Optimized view vector.
   */
  private static Vector3f optimizeRandomized(OptimizerOptions options, List<Vector3f> normals, Vector3f viewVector) {
    var t0 = System.currentTimeMillis();
    var iterations = options.iterations;
    var maxEntropy = calculateEntropy(options, normals, viewVector);
    var maxEntropyViewVector = viewVector;
    var ic = 0;
    for (ic = 0; ic < iterations; ic++) {
      var randomViewVector = new Vector3f((float) (Math.random() - 0.5) * 2f, (float) (Math.random() - 0.5) * 2f,
          (float) (Math.random() - 0.5) * 2f);
      randomViewVector.normalize();
      // For Entropy calculation benchmarking comment out the following lines. (Logger is slow)
      // var t0_0 = System.nanoTime();
      var entropy = calculateEntropy(options, normals, randomViewVector);
      // var t1_0 = System.nanoTime();
      // LOGGER.trace("Entropy: {} for ViewVector: {} took {} ns", entropy, randomViewVector, t1_0 - t0_0);
      if (entropy > maxEntropy) {
        maxEntropy = entropy;
        maxEntropyViewVector = randomViewVector;
      }
    }
    var t1 = System.currentTimeMillis();
    LOGGER.trace(
        "Optimization took {} ms with {} iterations for {} normals, getting a max. Entropy of {}. Resulting in {} us/normal",
        t1 - t0, ic + 1, normals.size(), maxEntropy, (t1 - t0) * 1000L / (long) normals.size());
    return maxEntropyViewVector.mul(options.zoomOutFactor);
  }

  /**
   * Optimizes the view vector for the given model with the neighborhood EntropyOptimizer Strategy.
   *
   * @param options    Options for the optimizer and entropy calculation.
   * @param normals    List of normals of the model.
   * @param viewVector Initial view vector.
   * @return Optimized view vector.
   */
  private static Vector3f optimizeNeighborhood(OptimizerOptions options, List<Vector3f> normals, Vector3f viewVector) {

    return new Vector3f(0f, 0f, 1f);
  }

  /**
   * Wrapper for the entropy calculation strategy. Calculates the entropy of the model for the given view vector.
   *
   * @param options    Options for the optimizer and entropy calculation.
   * @param normals    List of normals of the model.
   * @param viewVector View vector.
   * @return Entropy of the model for the given view vector.
   */
  private static float calculateEntropy(OptimizerOptions options, List<Vector3f> normals, Vector3f viewVector) {
    var method = options.method;
    switch (method) {
      case RELATIVE_TO_TOTAL_AREA -> {
        return ModelEntropyOptimizer.calculateEntropyRelativeToTotalArea(normals, viewVector);
      }
      case RELATIVE_TO_TOTAL_AREA_WEIGHTED -> {
        return ModelEntropyOptimizer.calculateEntropyRelativeToTotalAreaWeighted(normals, viewVector, options);
      }
      case RELATIVE_TO_PROJECTED_AREA -> {
        return ModelEntropyOptimizer.calculateEntropyRelativeToProjectedArea(normals, viewVector);
      }
      default -> {
        return 0f;
      }
    }
  }

  /**
   * Calculates the entropy of the model for the given view vector relative to the projected area of the model.
   *
   * @param normals    List of normals of the model.
   * @param viewVector View vector.
   * @return Entropy of the model for the given view vector.
   */
  private static float calculateEntropyRelativeToProjectedArea(List<Vector3f> normals, Vector3f viewVector) {
    return 0f;
  }

  /**
   * Calculates the entropy of the model for the given view vector relative to the projected area of the model. see: <a href="https://scholar.google.ch/scholar?hl=de&as_sdt=0%2C5&as_vis=1&q=Viewpoint+selection+using+viewpoint+entrop&btnG=">Google Scholar</a> see: <a> href="https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=b854422671e5469373fd49fb3a916910b49a6920">Paper</a>
   *
   * @param normals    List of normals of the model.
   * @param viewVector View vector.
   * @return Entropy of the model for the given view vector.
   */
  private static float calculateEntropyRelativeToTotalArea_old(List<Vector3f> normals, Vector3f viewVector) {

    var areas = new ArrayList<Float>(normals.size());
    var projected = new ArrayList<Float>(normals.size());
    normals.stream().map(normal -> viewVector.dot(normal) / 2f).forEach(areas::add);
    areas.stream().map(area -> area > 0f ? area : 0f).forEach(projected::add);
    var totalArea = areas.stream().map(Math::abs).reduce(0f, Float::sum);
    var relativeProjected = projected.stream().map(x -> x / totalArea).toList();
    var logRelativeProjected = relativeProjected.stream().map(ModelEntropyOptimizer::log2).toList();
    assert relativeProjected.size() == logRelativeProjected.size();
    var result = IntStream.range(0, relativeProjected.size())
        .mapToObj(ic -> relativeProjected.get(ic) * logRelativeProjected.get(ic))
        .reduce(0f, Float::sum);
    var entropy = -result;
    return entropy;
  }

  /**
   * Calculates the entropy of the model for the given view vector relative to the projected area of the model. see: <a href="https://scholar.google.ch/scholar?hl=de&as_sdt=0%2C5&as_vis=1&q=Viewpoint+selection+using+viewpoint+entrop&btnG=">Google Scholar</a> see: <a> href="https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=b854422671e5469373fd49fb3a916910b49a6920">Paper</a>
   *
   * @param normals    List of normals of the model.
   * @param viewVector View vector.
   * @return Entropy of the model for the given view vector.
   */
  private static float calculateEntropyRelativeToTotalArea2(List<Vector3f> normals, Vector3f viewVector) {

    var areas = new float[normals.size()];
    var projected = new float[normals.size()];
    var totalArea = 0f;
    for (var ic = 0; ic < normals.size(); ic++) {
      areas[ic] = viewVector.dot(normals.get(ic)) / 2f;
      projected[ic] = Math.max(areas[ic], 0f);
      totalArea += Math.abs(areas[ic]);
    }
    areas = null;
    var entropy = 0f;
    for (var ic = 0; ic < normals.size(); ic++) {
      projected[ic] /= totalArea;
      entropy += projected[ic] * log2(projected[ic]);
    }

    return -entropy;
  }

  /**
   * Calculates the entropy of the model for the given view vector relative to the projected area of the model. see: <a href="https://scholar.google.ch/scholar?hl=de&as_sdt=0%2C5&as_vis=1&q=Viewpoint+selection+using+viewpoint+entrop&btnG=">Google Scholar</a> see: <a> href="https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=b854422671e5469373fd49fb3a916910b49a6920">Paper</a>
   *
   * @param normals    List of normals of the model.
   * @param viewVector View vector.
   * @param opts       Optimizer options.
   * @return Entropy of the model for the given view vector.
   */
  private static float calculateEntropyRelativeToTotalAreaWeighted(List<Vector3f> normals, Vector3f viewVector, OptimizerOptions opts) {

    var weightedNormals = new ArrayList<Vector3f>(normals.size());
    normals.stream().parallel().forEach(
        n -> {
          if (n.y > 0) {
            weightedNormals.add(new Vector3f(n.x, n.y * opts.yPosWeight, n.z));
          } else {
            weightedNormals.add(new Vector3f(n.x, n.y * opts.yNegWeight, n.z));
          }
        }
    );
    return calculateEntropyRelativeToTotalArea(weightedNormals, viewVector);
  }

  /**
   * Calculates the entropy of the model for the given view vector relative to the projected area of the model. see: <a href="https://scholar.google.ch/scholar?hl=de&as_sdt=0%2C5&as_vis=1&q=Viewpoint+selection+using+viewpoint+entrop&btnG=">Google Scholar</a> see: <a> href="https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=b854422671e5469373fd49fb3a916910b49a6920">Paper</a>
   *
   * @param normals    List of normals of the model.
   * @param viewVector View vector.
   * @return Entropy of the model for the given view vector.
   */
  private static float calculateEntropyRelativeToTotalArea(List<Vector3f> normals, Vector3f viewVector) {

    var areas = new float[normals.size()];
    var projected = new float[normals.size()];
    var totalArea = 0f;
    IntStream.range(0, normals.size()).parallel().forEach(ic -> {
      areas[ic] = viewVector.dot(normals.get(ic));
      projected[ic] = Math.max(areas[ic], 0f);
      areas[ic] = Math.abs(areas[ic]);
    });

    for (var ic = 0; ic < normals.size(); ic++) {
      totalArea += normals.get(ic).length();
    }

    var entropy = 0f;
    var finalTotalArea = totalArea;
    IntStream.range(0, normals.size()).parallel().forEach(ic -> {
      projected[ic] /= finalTotalArea;
      projected[ic] = projected[ic] * log2(projected[ic]);
    });

    for (var ic = 0; ic < normals.size(); ic++) {
      entropy += projected[ic];
    }

    return -entropy;
  }


  /**
   * Static values for log base 2, due to performance reasons.
   */
  private static final float LOG10OF2 = (float) Math.log10(2.0);

  /**
   * Calculates the logarithm of a number to base 2. If x is 0, 0 is returned.
   *
   * @param x The number to calculate the logarithm for.
   * @return log2(x)
   */
  private static float log2(float x) {
    if (x <= 0f) {
      return 0f;
    }
    return ((float) Math.log((double) x) / LOG10OF2);
  }

}
