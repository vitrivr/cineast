package org.vitrivr.cineast.core.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
  public java.lang.String toString() {
    return "IIIFConfig(imageApiVersion=" + this.getImageApiVersion() + ", baseUrl=" + this.getBaseUrl() + ", region=" + this.getRegion() + ", size=" + this.getSize() + ", rotation=" + this.getRotation() + ", quality=" + this.getQuality() + ", format=" + this.getFormat() + ", iiifItems=" + this.getIiifItems() + ")";
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

  public static class IIIFItem {

    @JsonProperty
    private String identifier;
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

    public IIIFItem() {
    }

    @Override
    public java.lang.String toString() {
      return "IIIFConfig.IIIFItem(identifier=" + this.getIdentifier() + ", region=" + this.getRegion() + ", size=" + this.getSize() + ", rotation=" + this.getRotation() + ", quality=" + this.getQuality() + ", format=" + this.getFormat() + ")";
    }

    public String getIdentifier() {
      return this.identifier;
    }

    @JsonProperty
    public void setIdentifier(final String identifier) {
      this.identifier = identifier;
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
  }
}