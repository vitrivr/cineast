package org.vitrivr.cineast.core.iiif.imageapi;

import lombok.Getter;

/**
 * Utility class to hold the Image API version along with some helpful methods for converting into different formats
 *
 * @author singaltanmay
 * @version 1.0
 * @created 14.06.21
 */
public class ImageApiVersion {

  public static final String IIIF_LEVEL_IMAGE_API_VERSION_2_1_1 = "http://iiif.io/api/image/2/level2.json";
  public static final String IIIF_LEVEL_IMAGE_API_VERSION_3_0 = "http://iiif.io/api/image/3/level1.json";

  /** Variable to hold the Image API version of this object */
  @Getter
  private final IMAGE_API_VERSION version;

  public ImageApiVersion(IMAGE_API_VERSION version) {
    this.version = version;
  }

  /**
   * Create a new {@link ImageApiVersion} object using the numeric string
   */
  public static ImageApiVersion fromNumericString(String numericString) {
    IMAGE_API_VERSION imageApiVersionEnum = numericStringToEnum(numericString);
    if (imageApiVersionEnum == null) {
      throw new IllegalArgumentException(numericString + " is not a supported IIIF Image API version");
    }
    return new ImageApiVersion(imageApiVersionEnum);
  }

  /**
   * Create a new {@link ImageApiVersion} object using IIIF level string usually obtained from the {@link ImageInformation}
   */
  public static ImageApiVersion fromIIIFImageLevelString(String imageLevelString) {
    IMAGE_API_VERSION imageApiVersionEnum = iiifImageLevelToEnum(imageLevelString);
    if (imageApiVersionEnum == null) {
      throw new IllegalArgumentException(imageLevelString + " is not a supported IIIF Image API level");
    }
    return new ImageApiVersion(imageApiVersionEnum);
  }

  /** Converts numeric version number string into an enum item */
  private static IMAGE_API_VERSION numericStringToEnum(String input) {
    if (input.equals("2.1.1")) {
      return IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE;
    } else if (input.equals("3.0") || input.equals("3.0.0")) {
      return IMAGE_API_VERSION.THREE_POINT_ZERO;
    }
    return null;
  }

  /** Converts IIIF Image API level string into an enum item */
  private static IMAGE_API_VERSION iiifImageLevelToEnum(String input) {
    if (input.equals(IIIF_LEVEL_IMAGE_API_VERSION_2_1_1)) {
      return IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE;
    } else if (input.equals(IIIF_LEVEL_IMAGE_API_VERSION_3_0)) {
      return IMAGE_API_VERSION.THREE_POINT_ZERO;
    }
    return null;
  }

  /** Returns the IIIF Image API Level string of this object's version */
  public String toIIIFImageLevelString() {
    if (this.version.equals(IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE)) {
      return IIIF_LEVEL_IMAGE_API_VERSION_2_1_1;
    } else if (this.version.equals(IMAGE_API_VERSION.THREE_POINT_ZERO)) {
      return IIIF_LEVEL_IMAGE_API_VERSION_3_0;
    }
    return null;
  }

  /** Returns the numeric string of this object's version */
  public String toNumericString() {
    switch (this.version) {
      case TWO_POINT_ONE_POINT_ONE:
        return "2.1.1";
      case THREE_POINT_ZERO:
        return "3.0";
    }
    return null;
  }

  /** Checks whether two {@link ImageApiVersion} represent the same {@link IMAGE_API_VERSION} */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ImageApiVersion) {
      return this.version.equals(((ImageApiVersion) obj).version);
    } else {
      return super.equals(obj);
    }
  }

  /**
   * Enum to hold the various Image Api specification versions supported by the builder
   */
  public enum IMAGE_API_VERSION {
    TWO_POINT_ONE_POINT_ONE,
    THREE_POINT_ZERO
  }
}
