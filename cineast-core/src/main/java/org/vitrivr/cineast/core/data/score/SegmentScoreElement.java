package org.vitrivr.cineast.core.data.score;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmentScoreElement extends AbstractScoreElement {
  public SegmentScoreElement(String segmentId, double score) {
    super(segmentId, score);
  }

  public String getSegmentId() {
    return this.getId();
  }

  /**
   * An id to map this {@link SegmentScoreElement} to a {@link org.vitrivr.cineast.core.data.query.containers.QueryContainer}.
   */
  private String queryContainerId;

  /**
   * Getter for the {@link org.vitrivr.cineast.core.data.query.containers.QueryContainer}'s ID, this {@link SegmentScoreElement} relates to
   * @return The {@link org.vitrivr.cineast.core.data.query.containers.QueryContainer}'s ID, to which this {@link SegmentScoreElement} relates to.
   */
  public String getQueryContainerId(){
    return queryContainerId;
  }

  /**
   * Setter for the {@link org.vitrivr.cineast.core.data.query.containers.QueryContainer}'s ID, this {@link SegmentScoreElement} relates to
   * @param queryContainerId The ID of the {@link org.vitrivr.cineast.core.data.query.containers.QueryContainer} this {@link SegmentScoreElement} relates to
   */
  public void setQueryContainerId(String queryContainerId){
    this.queryContainerId = queryContainerId;
  }

  @Override
  public SegmentScoreElement withScore(double newScore) {
    return new SegmentScoreElement(this.getSegmentId(), newScore);
  }
}
