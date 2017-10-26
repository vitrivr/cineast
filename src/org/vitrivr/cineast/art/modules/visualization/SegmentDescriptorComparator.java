package org.vitrivr.cineast.art.modules.visualization;

import java.util.Comparator;

import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;

/**
 * Created by sein on 07.09.16.
 */
public class SegmentDescriptorComparator implements Comparator<SegmentDescriptor> {
  @Override
  public int compare(SegmentDescriptor a, SegmentDescriptor b) {
    return a.getSequenceNumber() < b.getSequenceNumber() ? -1 : a.getSequenceNumber() == b.getSequenceNumber() ? 0 : 1;
  }
}