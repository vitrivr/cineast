package org.vitrivr.cineast.core.temporal.sequential;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.vitrivr.cineast.core.temporal.ScoredSegment;

/**
 * Class to store a sequential scoring path.
 *
 * <p>Contains the score and segments and other relevant information for the path</p>
 *
 * @author vGsteiger
 * @created 07.05.2021
 */
public class SequentialPath implements Comparable<SequentialPath> {

  private final String objectId;
  private double score;
  private final List<ScoredSegment> segments;
  private int lastContainerId;
  private final float startAbs;
  private float currentEndAbs;

  /**
   * Constructor to initiate a sequential path.
   */
  public SequentialPath(String objectId, ScoredSegment initSegment) {
    this.objectId = objectId;
    this.segments = new ArrayList<>();

    this.lastContainerId = -1;
    this.addSegment(initSegment);
    this.lastContainerId = initSegment.getContainerId();
    this.startAbs = initSegment.getStartAbs();
  }

  /**
   * Copy constructor to create a new instance of a sequential path.
   */
  public SequentialPath(SequentialPath sequentialPath) {
    this.objectId = sequentialPath.getObjectId();
    this.segments = new ArrayList<>(sequentialPath.segments);
    this.lastContainerId = sequentialPath.getCurrentContainerId();
    this.score = sequentialPath.getScore();
    this.startAbs = sequentialPath.getStartAbs();
    this.currentEndAbs = sequentialPath.getCurrentEndAbs();
  }

  /**
   * Add a new scored segment to the path.
   */
  public boolean addSegment(ScoredSegment segment) {
    if (segment.getContainerId() > lastContainerId) {
      this.segments.add(segment);
      this.lastContainerId = segment.getContainerId();
      this.score += segment.getScore();
      this.currentEndAbs = segment.getEndAbs();
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
    return lastContainerId;
  }

  public List<ScoredSegment> getSegments() {
    return segments;
  }

  public List<String> getSegmentIds() {
    return segments.stream().map(ScoredSegment::getSegmentId).collect(Collectors.toList());
  }

  public ScoredSegment getCurrentLastSegment() {
    return this.segments.get(this.segments.size() - 1);
  }

  public float getStartAbs() {
    return startAbs;
  }

  public float getCurrentEndAbs() {
    return currentEndAbs;
  }

  @Override
  public int compareTo(@NotNull SequentialPath o) {
    return Double.compare(this.score, o.getScore());
  }
}
