package org.vitrivr.cineast.core.data.query.containers;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.util.MathHelper;

public abstract class QueryContainer implements SegmentContainer {

  private float weight = 1f;
  private String id = null, superId = null;

  /**
   * Weight used for relevance feedback
   */
  public float getWeight() {
    return this.weight;
  }

  public void setWeight(float weight) {
    if (Float.isNaN(weight)) {
      this.weight = 0f;
      return;
    }
    this.weight = MathHelper.limit(weight, -1f, 1f);
  }

  /**
   * Online: Id of the original query "box" (i.e. to which part of the query this container relates to).
   * Offline (i.e. Extraction): No clue, it does something...
   * @return
   */
  @Override
  public String getId() {
    return this.id == null ? "" : this.id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public boolean hasId() {
    return this.id != null;
  }


  @Override
  public void setSuperId(String id) {
    this.superId = id;
  }

  /**
   * Online: The parental query id, i.e. the id of the query to which this container belongs to
   * Offline (i.e. Extraction): No clue, it does something...
   */
  @Override
  public String getSuperId() {
    return this.superId;
  }

  @Override
  public String toString(){
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
