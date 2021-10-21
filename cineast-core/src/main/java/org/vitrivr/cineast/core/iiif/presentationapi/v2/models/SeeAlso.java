package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class SeeAlso {

  @JsonProperty("@id")
  private String atId;
  @JsonProperty
  private String format;
  @JsonProperty
  private String profile;

  public SeeAlso() {
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

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  @Override
  public String toString() {
    return "SeeAlso{" +
        "atId='" + atId + '\'' +
        ", format='" + format + '\'' +
        ", profile='" + profile + '\'' +
        '}';
  }
}
