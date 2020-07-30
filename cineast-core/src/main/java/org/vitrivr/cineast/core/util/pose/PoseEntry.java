package org.vitrivr.cineast.core.util.pose;

import ucar.nc2.Group;

class PoseEntry {

  private final Group group;
  int index;
  int startFrame;
  int endFrame;
  SparseCSRGroupReader reader;

  public PoseEntry(int index, Group group) {
    this.group = group;
    this.index = index;
    this.startFrame = (int)(long)group.findAttribute("start_frame").getNumericValue();
    this.endFrame = (int)(long)group.findAttribute("end_frame").getNumericValue();
    this.reader = new SparseCSRGroupReader(135, group);
  }
}
