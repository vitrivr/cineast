package org.vitrivr.cineast.core.data.tag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class IncompleteTag implements WeightedTag {

  private final String id, name, description;
  private final float weight;

  public IncompleteTag(String id, String name, String description) {
    this(id, name, description, 1f);
  }


  /**
   * Constructor for {@link IncompleteTag}. Used to create object from JSON.
   *
   * @param id          The ID of the {@link IncompleteTag}, required.
   * @param name        The name of {@link IncompleteTag}, optional.
   * @param description The description of {@link IncompleteTag}, optional.
   * @param weight      The weight {@link IncompleteTag}, optional, defaults to 1.0
   */
  @JsonCreator
  public IncompleteTag(@JsonProperty(value = "id", required = true) String id,
      @JsonProperty(value = "name") String name,
      @JsonProperty(value = "description") String description,
      @JsonProperty(value = "weight", defaultValue = "1.0f") Float weight) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.weight = (weight == null) ? 1f : weight;
  }

  public IncompleteTag(Tag t) {
    this(
        (t != null && t.hasId()) ? t.getId() : null,
        (t != null && t.hasName()) ? t.getName() : null,
        (t != null && t.hasDescription()) ? t.getDescription() : null,
        (t != null && t instanceof WeightedTag) ? ((WeightedTag) t).getWeight() : 1f
    );
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public boolean hasId() {
    return this.id != null && !this.id.isEmpty();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean hasName() {
    return this.name != null && !this.name.isEmpty();
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public boolean hasDescription() {
    return this.description != null && !this.description.isEmpty();
  }

  @Override
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
    IncompleteTag that = (IncompleteTag) o;
    return Float.compare(that.weight, weight) == 0 &&
        Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, weight);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
  }

}
