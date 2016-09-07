package org.vitrivr.cineast.art.modules.visualization;

import org.vitrivr.cineast.core.db.ShotLookup;

import java.util.Comparator;

/**
 * Created by sein on 07.09.16.
 */
public class SegmentDescriptorComparator implements Comparator<ShotLookup.ShotDescriptor> {
  @Override
  public int compare(ShotLookup.ShotDescriptor a, ShotLookup.ShotDescriptor b) {
    return Integer.parseInt(a.getShotId()) < Integer.parseInt(b.getShotId()) ? -1 : Integer.parseInt(a.getShotId()) == Integer.parseInt(b.getShotId()) ? 0 : 1;
  }
}