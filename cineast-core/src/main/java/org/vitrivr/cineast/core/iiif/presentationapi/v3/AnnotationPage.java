package org.vitrivr.cineast.core.iiif.presentationapi.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AnnotationPage {

  @JsonProperty
  public String id;
  @JsonProperty
  public String type;
  @JsonProperty
  public List<Annotation> items;
}
