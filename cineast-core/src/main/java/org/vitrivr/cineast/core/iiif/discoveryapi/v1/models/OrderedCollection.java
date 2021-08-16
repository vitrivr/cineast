package org.vitrivr.cineast.core.iiif.discoveryapi.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OrderedCollection {

  public static String TYPE_CREATE = "Create";
  public static String TYPE_ADD = "Add";
  public static String TYPE_UPDATE = "Update";
  public static String TYPE_REMOVE = "Remove";

  @JsonProperty("@context")
  private List<String> atContext;

  @JsonProperty
  private String id;

  @JsonProperty
  private String type;

  @JsonProperty
  private Long totalItems;

  @JsonProperty
  private IdTypeObject first;

  @JsonProperty
  private IdTypeObject last;

  @JsonProperty
  private List<IdTypeObject> partOf;

  public OrderedCollection() {
  }

  public List<String> getAtContext() {
    return atContext;
  }

  public void setAtContext(List<String> atContext) {
    this.atContext = atContext;
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

  public Long getTotalItems() {
    return totalItems;
  }

  public void setTotalItems(Long totalItems) {
    this.totalItems = totalItems;
  }

  public IdTypeObject getFirst() {
    return first;
  }

  public void setFirst(IdTypeObject first) {
    this.first = first;
  }

  public IdTypeObject getLast() {
    return last;
  }

  public void setLast(IdTypeObject last) {
    this.last = last;
  }

  public List<IdTypeObject> getPartOf() {
    return partOf;
  }

  public void setPartOf(List<IdTypeObject> partOf) {
    this.partOf = partOf;
  }

  @Override
  public String toString() {
    return "OrderedCollection{" +
        "atContext=" + atContext +
        ", id='" + id + '\'' +
        ", type='" + type + '\'' +
        ", totalItems=" + totalItems +
        ", first=" + first +
        ", last=" + last +
        ", partOf=" + partOf +
        '}';
  }
}
