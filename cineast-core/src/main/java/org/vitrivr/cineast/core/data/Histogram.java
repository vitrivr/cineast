package org.vitrivr.cineast.core.data;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.NoSuchElementException;

public abstract class Histogram implements ReadableFloatVector {

  protected double[] bins;
  protected TObjectIntHashMap<String> binNames = new TObjectIntHashMap<>();

  protected Histogram(int numberOfBins) {
    this.bins = new double[numberOfBins];
  }

  public Histogram normalize() {
    double sum = 0;
    for (int i = 0; i < this.bins.length; ++i) {
      sum += this.bins[i];
    }
    if (sum > 1) {
      for (int i = 0; i < this.bins.length; ++i) {
        this.bins[i] /= sum;
      }
    }
    return this;
  }

  public double getBin(String name) {
    if (!this.binNames.containsKey(name)) {
      throw new NoSuchElementException();
    }

    int index = this.binNames.get(name);
    return this.bins[index];
  }

  /**
   * @param hist Other histogram
   * @return L2-distance between this and hist or +Inf if histograms are incompatible
   */
  public double getDistance(Histogram hist) {
    if (!areCompatible(hist)) {
      return Double.POSITIVE_INFINITY;
    }
    double dist = 0;
    for (int i = 0; i < this.bins.length; ++i) {
      double d = this.bins[i] - hist.bins[i];
      dist += (d * d);
    }
    return Math.sqrt(dist);
  }

  public abstract boolean areCompatible(Histogram hist);


  public String toFeatureString() {
    StringBuffer buf = new StringBuffer();
    buf.append('<');
    for (int i = 0; i < bins.length; ++i) {
      buf.append(bins[i]);
      if (i < bins.length - 1) {
        buf.append(", ");
      }
    }
    buf.append('>');
    return buf.toString();
  }

  @Override
  public int getElementCount() {
    return this.bins.length;
  }

  @Override
  public float getElement(int num) {
    if (num >= 0 && num < this.bins.length) {
      return (float) this.bins[num];
    }
    throw new IndexOutOfBoundsException(
        num + " is not a valid bin index for a histogram with " + this.bins.length + " bins");
  }
}
