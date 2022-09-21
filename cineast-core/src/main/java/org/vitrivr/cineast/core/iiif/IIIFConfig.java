package org.vitrivr.cineast.core.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion;

/**
 * IIIF configuration used to fetch media files from remote servers.
 */
public class IIIFConfig {

  @JsonProperty
  private boolean keepImagesPostExtraction;
  @JsonProperty("items")
  private List<IIIFItem> iiifItems;
  @JsonProperty
  private List<String> manifestUrls;
  @JsonProperty
  private String orderedCollectionUrl;

  public List<String> getManifestUrls() {
    return manifestUrls;
  }

  public void setManifestUrls(List<String> manifestUrls) {
    this.manifestUrls = manifestUrls;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }

  public List<IIIFItem> getIiifItems() {
    return this.iiifItems;
  }

  @JsonProperty("items")
  public void setIiifItems(final List<IIIFItem> iiifItems) {
    this.iiifItems = iiifItems;
  }

  public boolean isKeepImagesPostExtraction() {
    return keepImagesPostExtraction;
  }

  public void setKeepImagesPostExtraction(boolean keepImagesPostExtraction) {
    this.keepImagesPostExtraction = keepImagesPostExtraction;
  }

  public String getOrderedCollectionUrl() {
    return orderedCollectionUrl;
  }

  public void setOrderedCollectionUrl(String orderedCollectionUrl) {
    this.orderedCollectionUrl = orderedCollectionUrl;
  }
}