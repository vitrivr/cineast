package org.vitrivr.cineast.core.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Data class to hold the JSON configuration parameters of a single Image API request
 */
public class IIIFItem {

  @JsonProperty
  public String identifier;
  @JsonProperty
  public String region;
  @JsonProperty
  public String size;
  @JsonProperty
  public Float rotation;
  @JsonProperty
  public String quality;
  @JsonProperty
  public String format;
}
