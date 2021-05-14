package org.vitrivr.cineast.core.temporal;

import org.jetbrains.annotations.NotNull;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

/**
 * Class to store the information of a scored segment.
 *
 * <p>Stores relevant information and score of a segment of a container</p>
 *
 * @author vGsteiger
 * @created 11.05.2021
 */
public class ScoredSegment implements Comparable<ScoredSegment> {

  private final String objectId;
  private final String segmentId;
  private final int containerId;
  private final float segmentLength;
  private final float startAbs;
  private final float endAbs;
  private double score;

  /**
   * Constructor to create a scored segment.
   */
  public ScoredSegment(MediaSegmentDescriptor mediaSegmentDescriptor, double score, int containerId, float segmentLength) {
    this.objectId = mediaSegmentDescriptor.getObjectId();
    this.segmentId = mediaSegmentDescriptor.getSegmentId();
    this.startAbs = mediaSegmentDescriptor.getStartabs();
    this.endAbs = mediaSegmentDescriptor.getEndabs();
    this.containerId = containerId;
    this.segmentLength = segmentLength;
    this.score = score;
  }

  /**
   * Copy constructor to create a new instance with the same values.
   */
  public ScoredSegment(ScoredSegment scoredSegment) {
    this.objectId = scoredSegment.getObjectId();
    this.segmentId = scoredSegment.getSegmentId();
    this.score = scoredSegment.getScore();
    this.containerId = scoredSegment.getContainerId();
    this.segmentLength = scoredSegment.getSegmentLength();
    this.startAbs = scoredSegment.getStartAbs();
    this.endAbs = scoredSegment.getEndAbs();
  }

  /**
   * Add a score to this segment, meaning this segment appeared more than once in a container.
   */
  public void addScore(StringDoublePair stringDoublePair) {
    if (stringDoublePair.key.equals(this.segmentId) && stringDoublePair.value > 0) {
      this.score += stringDoublePair.value;
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

  /*
  * The comparator first compares the container Id of the items.
  * If the container Id is larger, then the other segment clearly comes after the current segment.
  * However, as we use the Scored Segments in the Temporal scoring algorithms also in the TreeSets,
  * we don't want to loose item that have the same container Id. Therefore we then have to also
  * compare the segment ids to distinguish. Within a tree set there shouldn't be duplicates
  * (here same container Id and same segment Id).
  */
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
