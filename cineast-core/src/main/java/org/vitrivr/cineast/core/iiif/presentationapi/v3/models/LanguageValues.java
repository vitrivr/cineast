package org.vitrivr.cineast.core.iiif.presentationapi.v3.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Parsing object for IIIF language values.
 * <p>
 * WARNING: Currently only contains select few.
 */
public class LanguageValues {

  @JsonProperty
  public List<String> en;
  @JsonProperty
  public List<String> de;
  @JsonProperty
  public List<String> fr;
  @JsonProperty
  public List<String> none;
}
