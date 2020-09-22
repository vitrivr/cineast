package org.vitrivr.cineast.core.data;

import gnu.trove.list.array.TFloatArrayList;

import java.util.List;

public class FloatVectorImpl implements FloatVector {

  private TFloatArrayList list;

  public FloatVectorImpl(TFloatArrayList list) {
    this.list = list;
  }

  public FloatVectorImpl(Iterable<Float> iterable) {
    this();
    for (float f : iterable) {
      this.list.add(f);
    }
  }

  public FloatVectorImpl(float[] array) {
    this();
    for (float f : array) {
      this.list.add(f);
    }
  }

  public FloatVectorImpl() {
    this(new TFloatArrayList());
  }

  public FloatVectorImpl(List<Double> list) {
    this();
    for (double d : list) {
      this.list.add((float) d);
    }
  }

  public FloatVectorImpl(short[] array) {
    this();
    for (short s : array) {
      this.list.add(s);
    }
  }

  public FloatVectorImpl(double[] array) {
    this();
    for (double s : array) {
      this.list.add((float) s);
    }
  }

  @Override
  public int getElementCount() {
    return this.list.size();
  }

  @Override
  public float getElement(int num) {
    return this.list.get(num);
  }

  @Override
  public void setElement(int num, float val) {
    this.list.set(num, val);
  }

  public void add(float element) {
    this.list.add(element);
  }

  public String toFeatureString() {
    StringBuffer buf = new StringBuffer();
    buf.append('<');
    for (int i = 0; i < this.list.size(); ++i) {
      buf.append(list.get(i));
      if (i < this.list.size() - 1) {
        buf.append(", ");
      }
    }
    buf.append('>');
    return buf.toString();
  }

  @Override
  public String toString() {
    return this.toFeatureString();
  }
}
