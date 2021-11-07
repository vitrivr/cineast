package org.vitrivr.cineast.core.iiif.discoveryapi.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IdTypeObject {

  @JsonProperty
  private String id;

  @JsonProperty
  private String type;

  public IdTypeObject() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "{" + "id='" + id + '\'' + ", type='" + type + '\'' + '}';
  }
}
