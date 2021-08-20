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
  private String imageApiVersion;
  @JsonProperty(value = "imageApiUrl")
  private String baseUrl;
  @JsonProperty
  private String region;
  @JsonProperty
  private String size;
  @JsonProperty
  private Float rotation;
  @JsonProperty
  private String quality;
  @JsonProperty
  private String format;
  @JsonProperty("items")
  private List<IIIFItem> iiifItems;
  @JsonProperty
  private String manifestUrl;
  @JsonProperty
  private boolean keepImagesPostExtraction;

  public String getManifestUrl() {
    return manifestUrl;
  }

  public void setManifestUrl(String manifestUrl) {
    this.manifestUrl = manifestUrl;
  }


  public ImageApiVersion getImageApiVersion() {
    if (imageApiVersion == null) {
      throw new IllegalArgumentException("Image API Version is not defined!");
    }
    return ImageApiVersion.fromNumericString(imageApiVersion);
  }

  @JsonProperty
  public void setImageApiVersion(final String imageApiVersion) {
    this.imageApiVersion = imageApiVersion;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }

  public String getBaseUrl() {
    return this.baseUrl;
  }

  @JsonProperty(value = "imageApiUrl", required = true)
  public void setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getRegion() {
    return this.region;
  }

  @JsonProperty
  public void setRegion(final String region) {
    this.region = region;
  }

  public String getSize() {
    return this.size;
  }

  @JsonProperty
  public void setSize(final String size) {
    this.size = size;
  }

  public Float getRotation() {
    return this.rotation;
  }

  @JsonProperty
  public void setRotation(final Float rotation) {
    this.rotation = rotation;
  }

  public String getQuality() {
    return this.quality;
  }

  @JsonProperty
  public void setQuality(final String quality) {
    this.quality = quality;
  }

  public String getFormat() {
    return this.format;
  }

  @JsonProperty
  public void setFormat(final String format) {
    this.format = format;
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
}