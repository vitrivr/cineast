package org.vitrivr.cineast.core.util.pose;

import com.google.common.collect.PeekingIterator;
import java.util.Iterator;
import org.apache.commons.lang3.tuple.Pair;
import org.vitrivr.cineast.core.util.pose.PoseIterator.IndexedPose;

public class PoseIterator implements PeekingIterator<IndexedPose> {

  private final FrameIterator frameIterator;
  private Iterator<Pair<Integer, float[][]>> poseIterator;
  private int frameIdx;
  private IndexedPose curPose = null;

  public static class IndexedPose {

    public int frameIdx;
    public int poseIdx;
    public float[][] pose;

    public IndexedPose(int frameIdx, int poseIdx, float[][] pose) {
      this.frameIdx = frameIdx;
      this.poseIdx = poseIdx;
      this.pose = pose;
    }
  }

  public PoseIterator(FrameIterator frameIterator) {
    this.frameIterator = frameIterator;
    this.poseIterator = null;
    this.frameIdx = -1;
    fetchNext();
  }

  private boolean noMorePose() {
    return this.poseIterator == null || !this.poseIterator.hasNext();
  }

  private boolean refreshPoseIterator() {
    while (this.noMorePose()) {
      if (!this.frameIterator.hasNext()) {
        this.curPose = null;
        return false;
      }
      this.poseIterator = this.frameIterator.next();
      this.frameIdx++;
    }
    return true;
  }

  private void fetchNext() {
    while (true) {
      if (!this.refreshPoseIterator()) {
        this.curPose = null;
        return;
      }
      Pair<Integer, float[][]> idxPosePair = this.poseIterator.next();
      if (idxPosePair.getRight() != null) {
        this.curPose = new IndexedPose(
            this.frameIdx,
            idxPosePair.getLeft(),
            idxPosePair.getRight()
        );
        return;
      }
    }
  }

  @Override
  public IndexedPose peek() {
    return this.curPose;
  }

  @Override
  public boolean hasNext() {
    return this.curPose != null;
  }

  @Override
  public IndexedPose next() {
    IndexedPose yieldedPose = this.curPose;
    fetchNext();
    return yieldedPose;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove");
  }
}
