package org.vitrivr.cineast.core.data.query.containers;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.util.MathHelper;

public abstract class QueryContainer implements SegmentContainer {

  private float weight = 1f;
  private String id = null, superId = null, containerId = null;

  /**
   * Weight of this specific query container. Defaults to {@link #weight} (1f)
   */
  public float getWeight() {
    return this.weight;
  }

  /**
   * Set the weight of this query container. Can range from -1f (useful in relevance feedback) to +1f
   */
  public void setWeight(float weight) {
    if (Float.isNaN(weight)) {
      this.weight = 0f;
      return;
    }
    this.weight = MathHelper.limit(weight, -1f, 1f);
  }

  public String getContainerId() {
    return containerId;
  }

  public void setContainerId(String containerId) {
    this.containerId = containerId;
  }

  /**
   * Online: If this is set, it is assumed to be the id of a {@link MediaSegmentDescriptor}
   *
   * Offline: Id of the segment whose extraction is running
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
   * Online: If this is set, it is assumed to be the id of a {@link MediaObjectDescriptor}
   *
   * Offline: The {@link MediaObjectDescriptor} this segment belongs to.
   */
  @Override
  public String getSuperId() {
    return this.superId;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
