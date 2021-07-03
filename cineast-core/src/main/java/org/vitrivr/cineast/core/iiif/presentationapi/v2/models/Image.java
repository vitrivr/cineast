package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 23.06.21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {

  @JsonProperty("@id")
  private String atId;
  @JsonProperty("@type")
  private String atType;
  @JsonProperty
  private String motivation;
  @JsonProperty
  private Resource resource;
  @JsonProperty
  private String on;

  public Image() {
  }

  @Override
  public String toString() {
    return "Image{" +
        "atId='" + atId + '\'' +
        ", atType='" + atType + '\'' +
        ", motivation='" + motivation + '\'' +
        ", resource=" + resource +
        ", on='" + on + '\'' +
        '}';
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

  public String getMotivation() {
    return motivation;
  }

  public void setMotivation(String motivation) {
    this.motivation = motivation;
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public String getOn() {
    return on;
  }

  public void setOn(String on) {
    this.on = on;
  }
}
