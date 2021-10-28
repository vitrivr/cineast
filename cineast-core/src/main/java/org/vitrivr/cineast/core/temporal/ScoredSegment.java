package org.vitrivr.cineast.core.temporal;

import org.jetbrains.annotations.NotNull;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

/**
 * Class to store the information of a scored segment.
 *
 * <p>Stores relevant information and score of a segment of a container</p>
 * <p>Utilized in all instances of the {@link AbstractTemporalScoringAlgorithm} to add up all scores of a certain segment scored in a certain container</p>
 * <p>Provides functions to compare two scored segments with each other according to their container id and segment id</p>
 * <p>Each scored segment is the result of adding up all scores of all results from one SimilarityQuery concerning one segment Id</p>
 */
public class ScoredSegment implements Comparable<ScoredSegment> {

  private final String objectId;
  private final String segmentId;
  private final int containerId;
  private final float startAbs;
  private final float endAbs;
  private final int sequenceNumber;
  private final int end;
  private final int start;
  private double score;
  private double totalScore = 0;
  private double normalizer = 0;

  /**
   * Constructor to create a scored segment.
   */
  public ScoredSegment(MediaSegmentDescriptor mediaSegmentDescriptor, double score, int containerId) {
    this.objectId = mediaSegmentDescriptor.getObjectId();
    this.segmentId = mediaSegmentDescriptor.getSegmentId();
    this.start = mediaSegmentDescriptor.getStart();
    this.end = mediaSegmentDescriptor.getEnd();
    this.startAbs = mediaSegmentDescriptor.getStartabs();
    this.endAbs = mediaSegmentDescriptor.getEndabs();
    this.sequenceNumber = mediaSegmentDescriptor.getSequenceNumber();
    this.containerId = containerId;
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
    this.start = scoredSegment.getStart();
    this.end = scoredSegment.getEnd();
    this.startAbs = scoredSegment.getStartAbs();
    this.endAbs = scoredSegment.getEndAbs();
    this.sequenceNumber = scoredSegment.getSequenceNumber();
  }

  public int getEnd() {
    return end;
  }

  public int getStart() {
    return start;
  }

  /**
   * Add a score to this segment, meaning this segment appeared more than once in a container.
   */
  public void addScore(StringDoublePair stringDoublePair) {
    // Implementation note: currently uses averagepooling. We could also use maxpooling, or weighted pooling.
    if (stringDoublePair.key.equals(this.segmentId) && stringDoublePair.value > 0) {
      this.normalizer++;
      this.totalScore += stringDoublePair.value;
      this.score = totalScore / normalizer;
    }
  }

  public String getSegmentId() {
    return segmentId;
  }

  public int getSequenceNumber() {
    return this.sequenceNumber;
  }

  public double getScore() {
    return score;
  }

  public int getContainerId() {
    return containerId;
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
   * If the container Id is larger, then the scored segment with the according score should come first in a temporal sequence.
   * However, as we use the Scored Segments in the Temporal scoring algorithms also in the TreeSets, we don't want to loose item that have the same container Id. Therefore we then have to also compare the segment ids to distinguish. Within a tree set there shouldn't be duplicates (here same container Id and same segment Id).
   */
  @Override
  public int compareTo(@NotNull ScoredSegment o) {
    int comparison = Integer.compare(this.containerId, o.getContainerId());
    if (comparison != 0) {
      return comparison;
    }
    return Float.compare(this.sequenceNumber, o.getSequenceNumber());
  }
}
