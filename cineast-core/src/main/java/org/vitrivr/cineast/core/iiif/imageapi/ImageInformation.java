package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Interface defining the common functionality implemented by ImageInformation objects of every version of the Image API
 *
 * @author singaltanmay
 * @version 1.0
 * @created 13.06.21
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

  /** Get the actual width of the image */
  long getWidth();

  /** Get the actual height of the image */
  long getHeight();

  /** Get the {@link ImageApiVersion} of the ImageInformation */
  ImageApiVersion getImageApiVersion();

  /**
   * Inner class used to parse the Image Information JSON response.
   */
  @NoArgsConstructor
  @ToString
  @EqualsAndHashCode
  class SizesItem {

    @Getter
    @Setter
    @JsonProperty
    public Long width;

    @Getter
    @Setter
    @JsonProperty
    public Long height;

    public SizesItem(long width, long height) {
      this.width = width;
      this.height = height;
    }
  }

  /**
   * Inner class used to parse the Image Information JSON response.
   */
  @NoArgsConstructor
  @ToString
  @EqualsAndHashCode
  class TilesItem {

    @Getter
    @Setter
    @JsonProperty(required = true)
    public long width;

    @Getter
    @Setter
    @JsonProperty
    public long height;

    @Getter
    @Setter
    @JsonProperty(required = true)
    public List<Integer> scaleFactors;
  }

}
