package org.vitrivr.cineast.core.util.pose;

import java.util.Iterator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Group;
import ucar.nc2.Variable;

public class SparseCSRGroupReader implements Iterator<float[][]> {

  static private class IndPtrPairIterator implements Iterator<Pair<Integer, Integer>> {
    private final static int CHUNK_SIZE = 1024;
    private final ChunkingDatasetReader indptrReader;
    private int cur;
    private int chunkIndex;

    public IndPtrPairIterator(Variable indptr) {
      this.indptrReader = new ChunkingDatasetReader(indptr, CHUNK_SIZE);
      this.chunkIndex = 0;
      this.cur = this.next1();
    }

    @Override
    public boolean hasNext() {
      return (
          this.indptrReader.hasNext() ||
          this.chunkIndex + 1 < this.indptrReader.peek().getShape()[0]
      );
    }

    private int next1() {
      Array indArr = this.indptrReader.peek();
      int value = indArr.getInt(this.chunkIndex);
      this.chunkIndex++;
      if (this.chunkIndex >= CHUNK_SIZE) {
        this.chunkIndex -= CHUNK_SIZE;
        this.indptrReader.next();
      }
      return value;
    }

    @Override
    public Pair<Integer, Integer> next() {
      int prev = this.cur;
      this.cur = this.next1();
      return new ImmutablePair<>(prev, this.cur);
    }
  }

  final int DEPTH = 3;

  private final int numCols;
  private final Variable data;
  private final Variable indices;
  private final Variable indptr;
  private final Hdf5Slicer dataReader;
  private final Hdf5Slicer indicesReader;
  private final IndPtrPairIterator indptrPairIt;

  public SparseCSRGroupReader(int numCols, Group group) {
    this.numCols = numCols;
    this.data = group.findVariable("data");
    this.indices = group.findVariable("indices");
    this.indptr = group.findVariable("indptr");
    this.dataReader = new Hdf5Slicer(this.data);
    this.indicesReader = new Hdf5Slicer(this.indices);
    this.indptrPairIt = new IndPtrPairIterator(this.indptr);
  }

  @Override
  public boolean hasNext() {
    return this.indptrPairIt.hasNext();
  }

  public float[][] next() {
    if (!this.hasNext()) {
      return null;
    }
    Pair<Integer, Integer> dataIndexStartEnd = this.indptrPairIt.next();
    float[][] result = new float[this.numCols][DEPTH];
    int start = dataIndexStartEnd.getLeft();
    int extent = dataIndexStartEnd.getRight() - start;
    if (extent == 0) {
      return null;
    }
    Array data = this.dataReader.select1stDim(start, extent);
    Array indices = this.indicesReader.select1stDim(start, extent);
    for (int idx = 0; idx < extent; idx++) {
      short colIndex = indices.getShort(idx);
      result[colIndex] = (float[])data.slice(0, idx).get1DJavaArray(DataType.FLOAT);
    }
    return result;
  }
}
