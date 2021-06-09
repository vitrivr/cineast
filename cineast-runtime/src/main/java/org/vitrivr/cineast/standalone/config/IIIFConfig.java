package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * IIIF configuration used to fetch media files from remote servers.
 *
 * @author singaltanmay
 * @version 1.0
 * @created 28.05.21
 */
public class IIIFConfig {

  @JsonProperty(value = "url", required = true)
  private String baseUrl;

  @JsonProperty(value = "items")
  private List<IIIFItem> iiifItems;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public List<IIIFItem> getIiifItems() {
    return iiifItems;
  }

  public void setIiifItems(List<IIIFItem> iiifItems) {
    this.iiifItems = iiifItems;
  }

  public static class IIIFItem {

    private String identifier;
    private String rotation;

    public IIIFItem() {
    }

    @JsonProperty
    public String getIdentifier() {
      return identifier;
    }

    public void setIdentifier(String identifier) {
      this.identifier = identifier;
    }

    @JsonProperty
    public String getRotation() {
      return rotation;
    }

    public void setRotation(String rotation) {
      this.rotation = rotation;
    }
  }

}
