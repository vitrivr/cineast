package org.vitrivr.cineast.core.util.pose;

import java.util.ArrayList;
import ucar.nc2.Group;
import java.util.Iterator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class FrameIterator implements Iterator<Iterator<Pair<Integer, float[][]>>> {

  private final int endFrame;
  private final ArrayList<PoseEntry> poseEntries;
  private int curFrame;

  public FrameIterator(Group group) {
    this.curFrame = (int)(long)group.findAttribute("start_frame").getNumericValue();
    this.endFrame = (int)(long)group.findAttribute("end_frame").getNumericValue();
    this.poseEntries = makePoseEntries(group);
  }

  private ArrayList<PoseEntry> makePoseEntries(Group group) {
    ArrayList<PoseEntry> poseEntries = new ArrayList<>();
    int index = 0;
    while (true) {
      String poseKey = "pose" + index;
      Group childGroup = group.findGroup(poseKey);
      if (childGroup == null) {
        break;
      }
      PoseEntry poseEntry = new PoseEntry(index, childGroup);
      poseEntries.add(poseEntry);
      index++;
    }
    return poseEntries;
  }

  @Override
  public boolean hasNext() {
    return this.curFrame < this.endFrame;
  }

  @Override
  public Iterator<Pair<Integer, float[][]>> next() {
    int resultFrame = this.curFrame;
    this.curFrame++;
    return this.poseEntries.stream().filter(
        poseEntry ->
            resultFrame >= poseEntry.startFrame &&
            resultFrame < poseEntry.endFrame
    ).map(poseEntry ->
        (Pair<Integer, float[][]>)(
          new ImmutablePair<>(
              poseEntry.index,
              poseEntry.reader.next()
          )
        )
    ).iterator();
  }
}
