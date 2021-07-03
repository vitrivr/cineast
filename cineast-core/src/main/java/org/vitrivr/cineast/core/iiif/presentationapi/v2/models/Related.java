package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *   @author singaltanmay
 *   @version 1.0
 *   @created 23.06.21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Related {

  @JsonProperty("@id")
  private String atId;
  @JsonProperty
  private String format;

  public Related() {
  }

  @Override
  public String toString() {
    return "Related{" +
        "atId='" + atId + '\'' +
        ", format='" + format + '\'' +
        '}';
  }

  public String getAtId() {
    return atId;
  }

  public void setAtId(String atId) {
    this.atId = atId;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }
}
