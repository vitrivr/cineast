package org.vitrivr.cineast.core.temporal;

import org.jetbrains.annotations.NotNull;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

public class ScoredSegment implements Comparable<ScoredSegment> {

  private final String objectId;
  private final String segmentId;
  private final int containerId;
  private final float segmentLength;
  private final float startAbs;
  private final float endAbs;
  private double score;

  public ScoredSegment(MediaSegmentDescriptor mediaSegmentDescriptor, double score, int containerId, float segmentLength) {
    this.objectId = mediaSegmentDescriptor.getObjectId();
    this.segmentId = mediaSegmentDescriptor.getSegmentId();
    this.startAbs = mediaSegmentDescriptor.getStartabs();
    this.endAbs = mediaSegmentDescriptor.getEndabs();
    this.containerId = containerId;
    this.segmentLength = segmentLength;
    this.score = score;
  }

  public ScoredSegment(ScoredSegment scoredSegment) {
    this.objectId = scoredSegment.getObjectId();
    this.segmentId = scoredSegment.getSegmentId();
    this.score = scoredSegment.getScore();
    this.containerId = scoredSegment.getContainerId();
    this.segmentLength = scoredSegment.getSegmentLength();
    this.startAbs = scoredSegment.getStartAbs();
    this.endAbs = scoredSegment.getEndAbs();
  }

  public boolean addScore(StringDoublePair stringDoublePair) {
    if (stringDoublePair.key.equals(this.segmentId) && stringDoublePair.value > 0) {
      this.score += stringDoublePair.value;
      return true;
    } else {
      return false;
    }
  }

  public String getSegmentId() {
    return segmentId;
  }

  public double getScore() {
    return score;
  }

  public int getContainerId() {
    return containerId;
  }

  public float getSegmentLength() {
    return segmentLength;
  }

  public String getObjectId() {
    return objectId;
  }

  public float getStartAbs() {
    return startAbs;
  }

  public float getEndAbs() {
    return endAbs;
  }

  @Override
  public int compareTo(@NotNull ScoredSegment o) {
    int comparison = Integer.compare(this.containerId, o.getContainerId());
    if (comparison != 0) {
      return comparison;
    } else {
      return this.segmentId.compareTo(o.getSegmentId());
    }
  }
}
