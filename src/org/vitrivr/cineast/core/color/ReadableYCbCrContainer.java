package org.vitrivr.cineast.core.color;

public class ReadableYCbCrContainer extends
    AbstractColorContainer<ReadableYCbCrContainer> implements Cloneable {

  protected int y, cb, cr;

  public ReadableYCbCrContainer(int Y, int Cb, int Cr) {
    this.y = Y;
    this.cb = Cb;
    this.cr = Cr;
  }

  @Override
  public float getElement(int num) {
    switch (num) {
      case 0: return y / 255f;
      case 1: return cb / 255f;
      case 2: return cr / 255f;
      default: throw new IndexOutOfBoundsException(num + ">= 3");
    }
  }

  public String toFeatureString() {
    return '<' + this.y + ", " + this.cb + ", " + this.cr + '>';
  }

  public int getY() {
    return this.y;
  }

  public int getCb() {
    return this.cb;
  }

  public int getCr() {
    return this.cr;
  }

  @Override
  public float[] toArray(float[] arr) {
    if (arr != null && arr.length == 3) {
      arr[0] = y / 255f;
      arr[1] = cb / 255f;
      arr[2] = cr / 255f;
      return arr;
    }
    return new float[]{y, cb, cr};
  }
}
