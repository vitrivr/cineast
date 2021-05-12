package org.vitrivr.cineast.core.temporal.sequential;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.temporal.ScoredSegment;

public class SequentialPath {

  private final String objectId;
  private double score;
  private final List<ScoredSegment> segments;
  private int currentContainerId;
  private float pathLength;

  public SequentialPath(String objectId, ScoredSegment initSegment) {
    this.objectId = objectId;
    this.segments = new ArrayList<>();

    this.currentContainerId = -1;
    this.addSegment(initSegment);
    this.currentContainerId = initSegment.getContainerId();
    this.pathLength = initSegment.getSegmentLength();
  }

  public SequentialPath(SequentialPath sequentialPath) {
    this.objectId = sequentialPath.getObjectId();
    this.segments = new ArrayList<>(sequentialPath.segments);
    this.currentContainerId = sequentialPath.getCurrentContainerId();
    this.score = sequentialPath.getScore();
  }

  public boolean addSegment(ScoredSegment segment) {
    if (segment.getContainerId() > currentContainerId) {
      this.segments.add(segment);
      this.currentContainerId = segment.getContainerId();
      this.score += segment.getScore();
      this.pathLength += segment.getSegmentLength();
      return true;
    } else {
      return false;
    }
  }

  public String getObjectId() {
    return objectId;
  }

  public double getScore() {
    return score;
  }

  public int getCurrentContainerId() {
    return currentContainerId;
  }

  public List<ScoredSegment> getSegments() {
    return segments;
  }

  public float getPathLength() {
    return pathLength;
  }

  public List<String> getSegmentIds() {
    return segments.stream().map(ScoredSegment::getSegmentId).collect(Collectors.toList());
  }

  public ScoredSegment getCurrentLastSegment() {
    return this.segments.get(this.segments.size() - 1);
  }
}
