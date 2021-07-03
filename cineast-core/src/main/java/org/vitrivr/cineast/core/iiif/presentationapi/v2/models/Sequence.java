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
public class Sequence {

  @JsonProperty("@id")
  private String atId;
  @JsonProperty("@type")
  private String atType;
  @JsonProperty
  private String label;
  @JsonProperty
  private List<Canvas> canvases;

  public Sequence() {
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

  public List<Canvas> getCanvases() {
    return canvases;
  }

  public void setCanvases(List<Canvas> canvases) {
    this.canvases = canvases;
  }

  @Override
  public String toString() {
    return "Sequence{" +
        "atId='" + atId + '\'' +
        ", atType='" + atType + '\'' +
        ", label='" + label + '\'' +
        ", canvases=" + canvases +
        '}';
  }
}
