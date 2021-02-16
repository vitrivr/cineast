package org.vitrivr.cineast.core.data.tag;

import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CompleteTag implements Tag {

  private final String id, name, description;

  public CompleteTag(String id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = (description == null) ? "" : description;
    if (this.id == null) {
      throw new NullPointerException("id cannot be null for " + this.toString());
    }

    if (this.id.isEmpty()) {
      throw new IllegalArgumentException("id cannot be empty for " + this.toString());
    }

    if (this.name == null) {
      throw new NullPointerException("name cannot be null for " + this.toString());
    }

    if (this.name.isEmpty()) {
      throw new IllegalArgumentException("name cannot be empty for " + this.toString());
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompleteTag that = (CompleteTag) o;
    return id.equals(that.id) &&
        name.equals(that.name) &&
        description.equals(that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description);
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
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

}
