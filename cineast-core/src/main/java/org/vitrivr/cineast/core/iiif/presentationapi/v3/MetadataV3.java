package org.vitrivr.cineast.core.iiif.presentationapi.v3;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataV3 {

  @JsonProperty
  private LanguageValues label;
  @JsonProperty
  private LanguageValues value;

}
