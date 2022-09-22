package org.vitrivr.cineast.core.iiif.presentationapi.v3;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Annotation {

  @JsonProperty
  public String id;
  @JsonProperty
  public String type;
  @JsonProperty
  public String motivation;
  @JsonProperty
  public Body body;
  @JsonProperty
  public String target;
}
