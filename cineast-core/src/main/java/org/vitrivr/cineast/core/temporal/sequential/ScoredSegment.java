package org.vitrivr.cineast.core.temporal.sequential;

import org.jetbrains.annotations.NotNull;
import org.vitrivr.cineast.core.data.StringDoublePair;

public class ScoredSegment implements Comparable<ScoredSegment> {

  private final String segmentId;
  private double score;
  private final int containerId;
  private final float segmentLength;

  public ScoredSegment(String segmentId, double score, int containerId, float segmentLength) {
    this.segmentId = segmentId;
    this.score = score;
    this.containerId = containerId;
    this.segmentLength = segmentLength;
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
