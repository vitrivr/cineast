package org.vitrivr.cineast.core.color;

public class ReadableHSVContainer extends AbstractColorContainer<ReadableHSVContainer> implements
    Cloneable {

  protected float h, s, v;

  public ReadableHSVContainer(float h, float s, float v) {
    this.h = h;
    this.s = s;
    this.v = v;
  }

  public ReadableHSVContainer(double h, double s, double v) {
    this((float) h, (float) s, (float) v);
  }

  @Override
  public float getElement(int num) {
    switch (num) {
      case 0: return h;
      case 1: return s;
      case 2: return v;
      default: throw new IndexOutOfBoundsException(num + ">= 3");
    }
  }


  public String toFeatureString() {
    return "<" + h + ", " + s + ", " + v + ">";
  }

  @Override
  public String toString() {
    return "HSVContainer(" + h + ", " + s + ", " + v + ")";
  }

  public float getH() {
    return this.h;
  }

  public float getS() {
    return this.s;
  }

  public float getV() {
    return this.v;
  }
}
