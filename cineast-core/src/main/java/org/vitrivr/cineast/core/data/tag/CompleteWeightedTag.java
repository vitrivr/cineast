package org.vitrivr.cineast.core.data.tag;

import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CompleteWeightedTag implements WeightedTag {

  private final String id, name, description;
  private final float weight;

  public CompleteWeightedTag(String id, String name, String description, float weight) {
    this.id = id;
    this.name = name;
    this.description = (description == null) ? "" : description;
    this.weight = weight;
    if (this.id == null) {
      throw new NullPointerException("id cannot be null");
    }

    if (this.id.isEmpty()) {
      throw new IllegalArgumentException("id cannot be empty");
    }

    if (this.name == null) {
      throw new NullPointerException("name cannot be null");
    }

    if (this.name.isEmpty()) {
      throw new IllegalArgumentException("name cannot be empty");
    }

    if (this.weight > 1f || this.weight < 0f) {
      throw new IllegalArgumentException("weight " + this.weight + " outside of range (0,1)");
    }

  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean hasId() {
    return true;
  }

  @Override
  public boolean hasName() {
    return true;
  }

  @Override
  public boolean hasDescription() {
    return !this.description.isEmpty();
  }

  public float getWeight() {
    return this.weight;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompleteWeightedTag that = (CompleteWeightedTag) o;
    return Float.compare(that.weight, weight) == 0 &&
        id.equals(that.id) &&
        name.equals(that.name) &&
        description.equals(that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, weight);
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }

}
