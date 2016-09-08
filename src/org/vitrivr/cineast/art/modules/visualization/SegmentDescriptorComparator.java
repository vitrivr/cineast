package org.vitrivr.cineast.art.modules.visualization;

import org.vitrivr.cineast.core.db.SegmentLookup;

import java.util.Comparator;

/**
 * Created by sein on 07.09.16.
 */
public class SegmentDescriptorComparator implements Comparator<SegmentLookup.SegmentDescriptor> {
  @Override
  public int compare(SegmentLookup.SegmentDescriptor a, SegmentLookup.SegmentDescriptor b) {
    return Integer.parseInt(a.getShotId()) < Integer.parseInt(b.getShotId()) ? -1 : Integer.parseInt(a.getShotId()) == Integer.parseInt(b.getShotId()) ? 0 : 1;
  }
}