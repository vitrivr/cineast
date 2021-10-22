package org.vitrivr.cineast.core.data.score;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmentScoreElement extends AbstractScoreElement {
  public SegmentScoreElement(String segmentId, double score) {
    super(segmentId, score);
  }

  public String getSegmentId() {
    return this.getId();
  }

  /**
   * An id to map this {@link SegmentScoreElement} to a {@link AbstractQueryTermContainer}.
   */
  private String queryContainerId;

  /**
   * Getter for the {@link AbstractQueryTermContainer}'s ID, this {@link SegmentScoreElement} relates to
   * @return The {@link AbstractQueryTermContainer}'s ID, to which this {@link SegmentScoreElement} relates to.
   */
  public String getQueryContainerId(){
    return queryContainerId;
  }

  /**
   * Setter for the {@link AbstractQueryTermContainer}'s ID, this {@link SegmentScoreElement} relates to
   * @param queryContainerId The ID of the {@link AbstractQueryTermContainer} this {@link SegmentScoreElement} relates to
   */
  public void setQueryContainerId(String queryContainerId){
    this.queryContainerId = queryContainerId;
  }

  @Override
  public SegmentScoreElement withScore(double newScore) {
    return new SegmentScoreElement(this.getSegmentId(), newScore);
  }
}
