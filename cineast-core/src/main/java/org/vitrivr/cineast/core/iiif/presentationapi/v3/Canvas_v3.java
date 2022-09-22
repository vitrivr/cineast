package org.vitrivr.cineast.core.iiif.presentationapi.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Canvas_v3 {

  @JsonProperty
  public String id;
  @JsonProperty
  public String type;
  @JsonProperty
  public Object label;
  @JsonProperty
  public Integer width;
  @JsonProperty
  public Integer height;
  @JsonProperty
  public List<AnnotationPage> items;
  @JsonProperty
  public Object annotations;
  @JsonProperty
  public Object metadata;
  @JsonProperty
  public Object thumbnail;
}
