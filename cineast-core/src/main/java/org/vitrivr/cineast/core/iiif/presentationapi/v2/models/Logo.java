package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *   @author singaltanmay
 *   @version 1.0
 *   @created 23.06.21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Logo {

  @JsonProperty("@id")
  private String atId;

  @JsonProperty
  private Service service;

  public Logo() {
  }

  public String getAtId() {
    return atId;
  }

  public void setAtId(String atId) {
    this.atId = atId;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  @Override
  public String toString() {
    return "Logo{" +
        "atId='" + atId + '\'' +
        ", service=" + service +
        '}';
  }

}
