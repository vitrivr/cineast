package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class Rendering {

  @JsonProperty("@id")
  private String atId;
  @JsonProperty
  private String label;
  @JsonProperty
  private String format;
  @JsonProperty("@type")
  private String type;

  public Rendering() {
  }

  @Override
  public String toString() {
    return "Rendering{" +
        "atId='" + atId + '\'' +
        ", label='" + label + '\'' +
        ", format='" + format + '\'' +
        ", type='" + type + '\'' +
        '}';
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getAtId() {
    return atId;
  }

  public void setAtId(String atId) {
    this.atId = atId;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }
}
