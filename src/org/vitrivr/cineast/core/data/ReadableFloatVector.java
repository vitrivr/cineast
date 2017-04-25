package org.vitrivr.cineast.core.data;

import java.util.ArrayList;
import java.util.List;

public interface ReadableFloatVector {

  int getElementCount();

  float getElement(int num);

  /**
   * @param vector the vector
   * @return a new array containing the vector elements
   */
  static float[] toArray(ReadableFloatVector vector) {
    return toArray(vector, new float[vector.getElementCount()]);
  }

  /**
   * @param vector the vector
   * @param array the array to write into. If {@code array} does not match the vector size, a new
   *              array is generated instead.
   * @return an array containing the vector elements
   */
  static float[] toArray(ReadableFloatVector vector, float[] array) {
    int size = vector.getElementCount();
    if (array.length != size) {
      array = new float[size];
    }
    for (int i = 0; i < size; ++i) {
      array[i] = vector.getElement(i);
    }
    return array;
  }

  /**
   * @param vector the vector
   * @return a new list containing the vector elements
   */
  static List<Float> toList(ReadableFloatVector vector) {
    return toList(vector, new ArrayList<>(vector.getElementCount()));
  }

  /**
   * @param vector the vector
   * @param list the list to write into. Note that existing elements are removed from {@code list}.
   * @return the list containing the vector elements
   */
  static List<Float> toList(ReadableFloatVector vector, List<Float> list) {
    list.clear();
    for (int i = 0; i < vector.getElementCount(); ++i) {
      list.add(vector.getElement(i));
    }
    return list;
  }

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
