package org.vitrivr.cineast.explorative;

import java.io.Serializable;

public class HCTFloatVectorValue
    implements Comparable<HCTFloatVectorValue>, Serializable, Printable {

  private static final long serialVersionUID = 5908031484648901716L;
  private final float[] vector;
  private final String id;

  public HCTFloatVectorValue(float[] vector, String id) {
    this.vector = vector;
    this.id = id;
  }

  public float[] getVector() {
    return vector;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public int compareTo(HCTFloatVectorValue o) {
    if (id.hashCode() > o.getId().hashCode()) {
      return 1;
    }
    if (id.hashCode() < o.getId().hashCode()) {
      return -1;
    }
    return 0;
  }

  @Override
  public String printHtml() {
    return "<img class=\"thumb\" src=\"thumbnails/"
        + id + ".jpg\" />";
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (float v : vector) {
      stringBuilder.append(v + ", ");
    }
    if (stringBuilder.length() >= 2) {
      stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
    }
    return stringBuilder.toString();
  }
}
