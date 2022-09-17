package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.data.Pair;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata_v2 {

  @JsonProperty
  private Object label;
  @JsonProperty
  private String value;

  public Metadata_v2() {
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

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
