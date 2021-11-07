package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class used to serialize and deserialize the metadata -> label field of a {@link Manifest}
 */
public class LabelItem {

  @JsonProperty("@language")
  private String atLanguage;

  @JsonProperty("@value")
  private String atValue;

  public LabelItem() {
  }

  public String getAtLanguage() {
    return atLanguage;
  }

  public void setAtLanguage(String atLanguage) {
    this.atLanguage = atLanguage;
  }

  public String getAtValue() {
    return atValue;
  }

  public void setAtValue(String atValue) {
    this.atValue = atValue;
  }

  @Override
  public String toString() {
    return "LabelItem{" +
        "atLanguage='" + atLanguage + '\'' +
        ", atValue='" + atValue + '\'' +
        '}';
  }
}
