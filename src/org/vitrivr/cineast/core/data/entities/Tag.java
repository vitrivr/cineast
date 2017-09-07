package org.vitrivr.cineast.core.data.entities;

public class Tag {

  private final String id, name, description;
  
  public Tag(String id, String name, String description){
    this.id = id;
    this.name = name;
    this.description = (description == null) ? "" : description;
    if(this.id == null){
      throw new NullPointerException("id cannot be null");
    }
    
    if(this.name == null){
      throw new NullPointerException("name cannot be null");
    }
  }
  
  public Tag(String id, String name){
    this(id, name, "");
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    Tag other = (Tag) obj;
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
    return true;
  }

  @Override
  public String toString() {
    return String.format("Tag [id=%s, name=%s, description=%s]", id, name, description);
  }
  
  
  
}
