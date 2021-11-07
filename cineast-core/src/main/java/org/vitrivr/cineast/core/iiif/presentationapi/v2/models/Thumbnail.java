package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class Thumbnail {

  @JsonProperty("@id")
  private String atId;
  @JsonProperty("@type")
  private String atType;
  @JsonProperty
  private String format;
  @JsonProperty
  private Service service;

  public Thumbnail() {
  }

  public String getAtId() {
    return atId;
  }

  public void setAtId(String atId) {
    this.atId = atId;
  }

  public String getAtType() {
    return atType;
  }

  public void setAtType(String atType) {
    this.atType = atType;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  @Override
  public String toString() {
    return "Thumbnail{" +
        "atId='" + atId + '\'' +
        ", atType='" + atType + '\'' +
        ", format='" + format + '\'' +
        ", service=" + service +
        '}';
  }

}
