package org.vitrivr.cineast.core.data;

import java.util.List;

public interface ReadableFloatVector {

  int getElementCount();

  float getElement(int num);

  /**
   * maps the vector to a float array
   *
   * @param arr the array to write into. If arr is null or does not have the correct length, a new
   * array is generated instead
   */
  float[] toArray(float[] arr);

  List<Float> toList(List<Float> list);

  static double getEuclideanDistance(ReadableFloatVector first, ReadableFloatVector second) {
    int len = Math.min(first.getElementCount(), second.getElementCount());
    double sum = 0d;
    for (int i = 0; i < len; ++i) {
      double diff = first.getElement(i) - second.getElement(i);
      sum += diff * diff;
    }
    return Math.sqrt(sum);
  }
}
