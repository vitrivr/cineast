package org.vitrivr.cineast.core.iiif.presentationapi.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Body {

  @JsonProperty
  public String id;
  @JsonProperty
  public String type;
}
