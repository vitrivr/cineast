package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 23.06.21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Service {

  @JsonProperty("@context")
  private String atContext;
  @JsonProperty("@id")
  private String atId;
  @JsonProperty
  private String profile;

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

  @Override
  public String toString() {
    return "Service{" +
        "atContext='" + atContext + '\'' +
        ", atId='" + atId + '\'' +
        ", profile='" + profile + '\'' +
        '}';
  }
}
