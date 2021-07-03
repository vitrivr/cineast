package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 23.06.21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resource {

  @JsonProperty("@id")
  private String atId;
  @JsonProperty("@type")
  private String atType;
  @JsonProperty
  private String format;
  @JsonProperty
  private long width;
  @JsonProperty
  private long height;
  @JsonProperty
  private Service service;

  public Resource() {
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

  public long getWidth() {
    return width;
  }

  public void setWidth(long width) {
    this.width = width;
  }

  public long getHeight() {
    return height;
  }

  public void setHeight(long height) {
    this.height = height;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  @Override
  public String toString() {
    return "Resource{" +
        "atId='" + atId + '\'' +
        ", atType='" + atType + '\'' +
        ", format='" + format + '\'' +
        ", width=" + width +
        ", height=" + height +
        ", service=" + service +
        '}';
  }
}
