package org.vitrivr.cineast.core.data.tag;

public class IncompleteTag implements WeightedTag {

  private final String id, name, description;
  private final float weight;
  
  public IncompleteTag(String id, String name, String description){
    this(id, name, description, 1f);
  }
  
  public IncompleteTag(String id, String name, String description, float weight){
    this.id = id;
    this.name = name;
    this.description = description;
    this.weight = weight;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + Float.floatToIntBits(weight);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IncompleteTag other = (IncompleteTag) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (Float.floatToIntBits(weight) != Float.floatToIntBits(other.weight))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return String.format("IncompleteTag [id=%s, name=%s, description=%s, weight=%s]", id, name,
        description, weight);
  }

}
