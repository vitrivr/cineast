package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

    @Override
    public String toString() {
      return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

    @Override
    public boolean equals(final Object o) {
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
      final Object this$width = this.getWidth();
      final Object other$width = other.getWidth();
      if (!Objects.equals(this$width, other$width)) {
        return false;
      }
      final Object this$height = this.getHeight();
      final Object other$height = other.getHeight();
      return Objects.equals(this$height, other$height);
    }

    protected boolean canEqual(final Object other) {
      return other instanceof ImageInformation.SizesItem;
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

    @Override
    public String toString() {
      return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

    @Override
    public boolean equals(final Object o) {
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
      final Object this$scaleFactors = this.getScaleFactors();
      final Object other$scaleFactors = other.getScaleFactors();
      return Objects.equals(this$scaleFactors, other$scaleFactors);
    }

    protected boolean canEqual(final Object other) {
      return other instanceof ImageInformation.TilesItem;
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
