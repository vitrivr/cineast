package org.vitrivr.cineast.core.iiif.imageapi;

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
  Integer getWidth();

  /** Get the actual height of the image */
  Integer getHeight();

  /** Get the {@link ImageApiVersion} of the ImageInformation */
  ImageApiVersion getImageApiVersion();

}
