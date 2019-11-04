package org.vitrivr.cineast.core.util.distance;

import java.util.Comparator;

public class FloatArrayDistanceComparator implements Comparator<float[]> {

  private final float[] query;
  private final FloatArrayDistance distance;

  protected FloatArrayDistanceComparator(float[] query, FloatArrayDistance distance) {
    if (distance == null) {
      throw new NullPointerException("distance can not be null");
    }
    this.distance = distance;
    if (query == null) {
      throw new NullPointerException("query can not be null");
    }
    if (query.length == 0) {
      throw new IllegalArgumentException("query can not be empty");
    }
    this.query = new float[query.length];
    System.arraycopy(query, 0, this.query, 0, query.length);
  }

  @Override
  public int compare(float[] o1, float[] o2) {
    int compare = Double.compare(this.distance.applyAsDouble(this.query, o1),
        this.distance.applyAsDouble(this.query, o2));
    if (compare != 0) {
      return compare;
    }
    int len = Math.min(o1.length, o1.length);
    for (int i = 0; i < len; ++i) {
      compare = Float.compare(o1[i], o2[i]);
      if (compare != 0) {
        return compare;
      }
    }
    return 0;
  }

}
