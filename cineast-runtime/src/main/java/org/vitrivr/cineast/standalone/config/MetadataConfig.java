package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import org.vitrivr.cineast.core.extraction.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.util.ReflectionHelper;


public class MetadataConfig {

  /**
   * Name of the MetadataExtractor. Must correspond to the simple-name or the FQN of the respective class.
   *
   * @see org.vitrivr.cineast.core.extraction.metadata.MetadataExtractor
   */
  private String name;

  /**
   * Properties that are being used to initialize the Extractor.
   *
   * @see org.vitrivr.cineast.core.features.extractor.Extractor
   */
  private HashMap<String, String> properties = new HashMap<>();

  @JsonProperty(required = true)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty
  public HashMap<String, String> getProperties() {
    return properties;
  }

  public void setProperties(HashMap<String, String> properties) {
    this.properties = properties;
  }

  @JsonIgnore
  public MetadataExtractor getMetadataExtractor() {
    return ReflectionHelper.newMetadataExtractor(this.name);
  }
}
