package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 23.06.21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Canvas {

  @JsonProperty("@id")
  private String atId;
  @JsonProperty("@type")
  private String atType;
  @JsonProperty
  private String label;
  @JsonProperty
  private long width;
  @JsonProperty
  private long height;
  @JsonProperty
  private List<Image> images;

  public Canvas() {
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

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
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

  public List<Image> getImages() {
    return images;
  }

  public void setImages(List<Image> images) {
    this.images = images;
  }

  @Override
  public String toString() {
    return "Canvas{" +
        "atId='" + atId + '\'' +
        ", atType='" + atType + '\'' +
        ", label='" + label + '\'' +
        ", width=" + width +
        ", height=" + height +
        ", images=" + images +
        '}';
  }
}
