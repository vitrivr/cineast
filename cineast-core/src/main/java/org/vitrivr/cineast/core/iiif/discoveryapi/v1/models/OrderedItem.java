package org.vitrivr.cineast.core.iiif.discoveryapi.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderedItem {

  @JsonProperty
  private String type;

  @JsonProperty
  private IdTypeObject object;

  @JsonProperty
  private String endTime;

  public OrderedItem() {
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public IdTypeObject getObject() {
    return object;
  }

  public void setObject(IdTypeObject object) {
    this.object = object;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  @Override
  public String toString() {
    return "OrderedItem{" +
        "type='" + type + '\'' +
        ", object=" + object +
        ", endTime='" + endTime + '\'' +
        '}';
  }
}
