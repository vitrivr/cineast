package org.vitrivr.cineast.core.util.pose;

import ucar.ma2.Array;
import ucar.nc2.Variable;

public class Hdf5Slicer {

  private final Variable dataSet;
  private final int ndims;
  public final int[] dimSizes;

  public Hdf5Slicer(Variable dataSet) {
    this.dataSet = dataSet;
    this.ndims = dataSet.getRank();
    this.dimSizes = dataSet.getShape();
  }

  public Array select1stDim(int start, int extent) {
    int[] fileStart = new int[this.ndims];
    int[] fileCounts = new int[this.ndims];
    fileStart[0] = start;
    fileCounts[0] = extent;
    for (int i = 1; i < ndims; i++) {
      fileStart[i] = 0;
      fileCounts[i] = this.dimSizes[i];
    }
    try {
      return this.dataSet.read(fileStart, fileCounts);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
