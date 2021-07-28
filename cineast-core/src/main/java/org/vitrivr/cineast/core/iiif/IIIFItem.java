package org.vitrivr.cineast.core.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**  Data class to hold the JSON configuration parameters of a single Image API request */
public class IIIFItem {

  @JsonProperty
  private String identifier;
  @JsonProperty
  private String region;
  @JsonProperty
  private String size;
  @JsonProperty
  private Float rotation;
  @JsonProperty
  private String quality;
  @JsonProperty
  private String format;

  public IIIFItem() {
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }

  public String getIdentifier() {
    return this.identifier;
  }

  @JsonProperty
  public void setIdentifier(final String identifier) {
    this.identifier = identifier;
  }

  public String getRegion() {
    return this.region;
  }

  @JsonProperty
  public void setRegion(final String region) {
    this.region = region;
  }

  public String getSize() {
    return this.size;
  }

  @JsonProperty
  public void setSize(final String size) {
    this.size = size;
  }

  public Float getRotation() {
    return this.rotation;
  }

  @JsonProperty
  public void setRotation(final Float rotation) {
    this.rotation = rotation;
  }

  public String getQuality() {
    return this.quality;
  }

  @JsonProperty
  public void setQuality(final String quality) {
    this.quality = quality;
  }

  public String getFormat() {
    return this.format;
  }

  @JsonProperty
  public void setFormat(final String format) {
    this.format = format;
  }
}
