package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.core.data.Pair;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {

  @JsonProperty
  private Object label;
  @JsonProperty
  private String value;

  public Metadata() {
  }

  @Override
  public String toString() {
    return "MetadataItem{" +
        "label='" + label + '\'' +
        ", value='" + value + '\'' +
        '}';
  }

  /**
   * Custom getter for getLabel that converts List<Object> into a Pair<String, List<LabelItem>>
   */
  public Pair<String, List<LabelItem>> getLabel() {
    if (this.label instanceof List) {
      return new Pair<>(null, (List<LabelItem>) this.label);
    } else if (this.label instanceof String) {
      return new Pair<>(((String) this.label), null);
    }
    return null;
  }

  public void setLabel(Object label) {
    this.label = label;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
