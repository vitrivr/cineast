package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Service {

  @JsonProperty("@context")
  private String atContext;
  @JsonProperty("@id")
  private String atId;
  @JsonProperty
  private String profile;
  @JsonProperty
  private String label;

  public Service() {
  }

  public String getAtContext() {
    return atContext;
  }

  public void setAtContext(String atContext) {
    this.atContext = atContext;
  }

  public String getAtId() {
    return atId;
  }

  public void setAtId(String atId) {
    this.atId = atId;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return "Service{" +
        "atContext='" + atContext + '\'' +
        ", atId='" + atId + '\'' +
        ", profile='" + profile + '\'' +
        ", label='" + label + '\'' +
        '}';
  }
}
