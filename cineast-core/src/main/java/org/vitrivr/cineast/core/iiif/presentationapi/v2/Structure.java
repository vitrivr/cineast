package org.vitrivr.cineast.core.iiif.presentationapi.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.data.Pair;

@JsonIgnoreProperties(ignoreUnknown = true)
public
class Structure {

  @JsonProperty("@type")
  private String atType;
  @JsonProperty
  private Object label;
  @JsonProperty
  private String viewingHint;
  @JsonProperty("@id")
  private String atId;
  @JsonProperty
  private List<String> ranges;
  @JsonProperty
  private String within;
  @JsonProperty
  private List<String> canvases;

  public Structure() {
  }

  @Override
  public String toString() {
    return "Structure{" +
        "atType='" + atType + '\'' +
        ", label='" + label + '\'' +
        ", viewingHint='" + viewingHint + '\'' +
        ", atId='" + atId + '\'' +
        ", ranges=" + ranges +
        ", within='" + within + '\'' +
        ", canvases=" + canvases +
        '}';
  }

  public String getAtType() {
    return atType;
  }

  public void setAtType(String atType) {
    this.atType = atType;
  }

  /**
   * Custom getter for getLabel that converts List<Object> into a Pair<String, List<LabelItem>>
   */
  public Pair<String, List<LabelItem>> getLabel() {
    if (this.label instanceof List<?>) {
      var list = ((List<?>) this.label).stream().filter(item -> item instanceof LabelItem).map(item -> (LabelItem) item).collect(Collectors.toList());
      return new Pair<>(null, list);
    } else if (this.label instanceof String) {
      return new Pair<>(((String) this.label), null);
    }
    return null;
  }

  public void setLabel(Object label) {
    this.label = label;
  }

  public String getViewingHint() {
    return viewingHint;
  }

  public void setViewingHint(String viewingHint) {
    this.viewingHint = viewingHint;
  }

  public String getAtId() {
    return atId;
  }

  public void setAtId(String atId) {
    this.atId = atId;
  }

  public List<String> getRanges() {
    return ranges;
  }

  public void setRanges(List<String> ranges) {
    this.ranges = ranges;
  }

  public String getWithin() {
    return within;
  }

  public void setWithin(String within) {
    this.within = within;
  }

  public List<String> getCanvases() {
    return canvases;
  }

  public void setCanvases(List<String> canvases) {
    this.canvases = canvases;
  }
}
