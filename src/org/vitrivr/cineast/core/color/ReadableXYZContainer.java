package org.vitrivr.cineast.core.color;

public class ReadableXYZContainer extends AbstractColorContainer<ReadableXYZContainer> implements
    Cloneable {

  protected float x, y, z;

  public ReadableXYZContainer(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public ReadableXYZContainer(double x, double y, double z) {
    this((float) x, (float) y, (float) z);
  }

  @Override
  public String toString() {
    return "XYZContainer(" + x + ", " + y + ", " + z + ")";
  }

  @Override
  public float getElement(int num) {
    switch (num) {
      case 0: return x;
      case 1: return y;
      case 2: return z;
      default: throw new IndexOutOfBoundsException(num + ">= 3");
    }
  }

  public String toFeatureString() {
    return "<" + x + ", " + y + ", " + z + ">";
  }

  public float getX() {
    return this.x;
  }

  public float getY() {
    return this.y;
  }

  public float getZ() {
    return this.z;
  }
}
