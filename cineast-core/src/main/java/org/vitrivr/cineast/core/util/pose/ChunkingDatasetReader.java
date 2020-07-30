package org.vitrivr.cineast.core.util.pose;

import com.google.common.collect.PeekingIterator;
import ucar.ma2.Array;
import ucar.nc2.Variable;

public class ChunkingDatasetReader implements PeekingIterator<Array> {

  /**
   * Reads a dataset in fixed chunks along the first axis. At each iteration, returns the number of
   * elements read and a raw pointer to the result.
   */
  private final Variable dataSet;
  private final Hdf5Slicer slicer;
  private final int chunkSize;
  private int position;
  private Array curResult;

  public ChunkingDatasetReader(Variable dataSet, int chunkSize) {
    this.dataSet = dataSet;
    this.slicer = new Hdf5Slicer(dataSet);
    this.chunkSize = chunkSize;
    this.position = 0;
    this.pullNext();
  }

  private void pullNext() {
    //System.out.printf("pullNext: %d %d\n", this.slicer.dimSizes[0], this.position);
    if (this.position >= this.slicer.dimSizes[0]) {
      this.curResult = null;
      return;
    }
    int extent = Math.min(this.chunkSize, this.slicer.dimSizes[0] - this.position);
    this.curResult = this.slicer.select1stDim(this.position, extent);
    this.position += extent;
  }

  @Override
  public Array peek() {
    //System.out.printf("peek: %s\n", this.curResult);
    return this.curResult;
  }

  @Override
  public boolean hasNext() {
    return this.curResult != null;
  }

  @Override
  public Array next() {
    Array savedResult = this.curResult;
    this.pullNext();
    return savedResult;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove");
  }
}
