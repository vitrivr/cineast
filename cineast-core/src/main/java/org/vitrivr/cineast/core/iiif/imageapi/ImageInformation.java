package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Interface defining the common functionality implemented by ImageInformation objects of every version of the Image API
 */
public interface ImageInformation {

  /**
   * @param feature String denoting a feature whose support needs to be checked
   * @return false if server has advertised it's supported features and the doesn't include this specific feature
   */
  boolean isFeatureSupported(String feature);

  /**
   * @param quality String denoting a quality whose support needs to be checked
   * @return false if server has advertised it's supported qualities and the doesn't include this specific quality
   */
  boolean isQualitySupported(String quality);

  /**
   * @param format String denoting a format whose support needs to be checked
   * @return false if server has advertised it's supported formats and the doesn't include this specific format
   */
  boolean isFormatSupported(String format);

  /**
   * Get the actual width of the image
   */
  long getWidth();

  /**
   * Get the actual height of the image
   */
  long getHeight();

  /**
   * Get the {@link ImageApiVersion} of the ImageInformation
   */
  ImageApiVersion getImageApiVersion();

  /**
   * Get the max width supported by server
   */
  Long getMaxWidth();

  /**
   * Get the max height supported by server
   */
  Long getMaxHeight();

  /**
   * Get the max area supported by server
   */
  Long getMaxArea();


  /**
   * Inner class used to parse the Image Information JSON response.
   */
  class SizesItem {

    @JsonProperty
    public Long width;

    @JsonProperty
    public Long height;

    public SizesItem(long width, long height) {
      this.width = width;
      this.height = height;
    }

    public SizesItem() {
    }

    @java.lang.Override
    public java.lang.String toString() {
      return "ImageInformation.SizesItem(width=" + this.getWidth() + ", height=" + this.getHeight() + ")";
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof ImageInformation.SizesItem)) {
        return false;
      }
      final ImageInformation.SizesItem other = (ImageInformation.SizesItem) o;
      if (!other.canEqual(this)) {
        return false;
      }
      final java.lang.Object this$width = this.getWidth();
      final java.lang.Object other$width = other.getWidth();
      if (this$width == null ? other$width != null : !this$width.equals(other$width)) {
        return false;
      }
      final java.lang.Object this$height = this.getHeight();
      final java.lang.Object other$height = other.getHeight();
      return this$height == null ? other$height == null : this$height.equals(other$height);
    }

    protected boolean canEqual(final java.lang.Object other) {
      return other instanceof ImageInformation.SizesItem;
    }

    @java.lang.Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final java.lang.Object $width = this.getWidth();
      result = result * PRIME + ($width == null ? 43 : $width.hashCode());
      final java.lang.Object $height = this.getHeight();
      result = result * PRIME + ($height == null ? 43 : $height.hashCode());
      return result;
    }

    public Long getWidth() {
      return this.width;
    }

    @JsonProperty
    public void setWidth(final Long width) {
      this.width = width;
    }

    public Long getHeight() {
      return this.height;
    }

    @JsonProperty
    public void setHeight(final Long height) {
      this.height = height;
    }
  }


  /**
   * Inner class used to parse the Image Information JSON response.
   */
  class TilesItem {

    @JsonProperty(required = true)
    public long width;
    @JsonProperty
    public long height;
    @JsonProperty(required = true)
    public List<Integer> scaleFactors;

    public TilesItem() {
    }

    @java.lang.Override
    public java.lang.String toString() {
      return "ImageInformation.TilesItem(width=" + this.getWidth() + ", height=" + this.getHeight() + ", scaleFactors=" + this.getScaleFactors() + ")";
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof ImageInformation.TilesItem)) {
        return false;
      }
      final ImageInformation.TilesItem other = (ImageInformation.TilesItem) o;
      if (!other.canEqual(this)) {
        return false;
      }
      if (this.getWidth() != other.getWidth()) {
        return false;
      }
      if (this.getHeight() != other.getHeight()) {
        return false;
      }
      final java.lang.Object this$scaleFactors = this.getScaleFactors();
      final java.lang.Object other$scaleFactors = other.getScaleFactors();
      return this$scaleFactors == null ? other$scaleFactors == null : this$scaleFactors.equals(other$scaleFactors);
    }

    protected boolean canEqual(final java.lang.Object other) {
      return other instanceof ImageInformation.TilesItem;
    }

    @java.lang.Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final long $width = this.getWidth();
      result = result * PRIME + (int) ($width >>> 32 ^ $width);
      final long $height = this.getHeight();
      result = result * PRIME + (int) ($height >>> 32 ^ $height);
      final java.lang.Object $scaleFactors = this.getScaleFactors();
      result = result * PRIME + ($scaleFactors == null ? 43 : $scaleFactors.hashCode());
      return result;
    }

    public long getWidth() {
      return this.width;
    }

    @JsonProperty(required = true)
    public void setWidth(final long width) {
      this.width = width;
    }

    public long getHeight() {
      return this.height;
    }

    @JsonProperty
    public void setHeight(final long height) {
      this.height = height;
    }

    public List<Integer> getScaleFactors() {
      return this.scaleFactors;
    }

    @JsonProperty(required = true)
    public void setScaleFactors(final List<Integer> scaleFactors) {
      this.scaleFactors = scaleFactors;
    }
  }
}
