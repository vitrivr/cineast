package org.vitrivr.cineast.core.iiif.imageapi;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 13.06.21
 */
public interface ImageInformation {

  String IMAGE_API_VERSION_2_1_1 = "http://iiif.io/api/image/2/level2.json";
  String IMAGE_API_VERSION_3_0 = "http://iiif.io/api/image/3/level1.json";

  default String getImageApiVersionString(IMAGE_API_VERSION apiVersion) {
    if (apiVersion.equals(IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE)) {
      return IMAGE_API_VERSION_2_1_1;
    } else if (apiVersion.equals(IMAGE_API_VERSION.THREE_POINT_ZERO)) {
      return IMAGE_API_VERSION_3_0;
    }
    return null;
  }

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

  Integer getWidth();

  Integer getHeight();

  /** Get the {@link IMAGE_API_VERSION} of the ImageInformation */
  IMAGE_API_VERSION getImageApiVersion();

  /**
   * Enum to hold the various Image Api specification versions supported by the builder
   */
  enum IMAGE_API_VERSION {
    TWO_POINT_ONE_POINT_ONE,
    THREE_POINT_ZERO
  }

}
