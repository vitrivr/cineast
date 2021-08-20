package org.vitrivr.cineast.core.iiif.imageapi;

/**
 * Utility class to hold the Image API version along with some helpful methods for converting into different formats
 */
public class ImageApiVersion {

  public static final String IMAGE_API_VERSION_2_1_1_COMPLIANCE_LEVEL_2 = "http://iiif.io/api/image/2/level2.json";
  public static final String IMAGE_API_VERSION_2_1_1_NUMERIC = "2.1.1";
  public static final String IMAGE_API_VERSION_3_0_COMPLIANCE_LEVEL_1 = "http://iiif.io/api/image/3/level1.json";
  public static final String IMAGE_API_VERSION_3_0_NUMERIC = "3.0";

  /**
   * Variable to hold the Image API version of this object
   */
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
   * Create a new {@link ImageApiVersion} object using the API compliance level string usually obtained from the {@link ImageInformation}
   */
  public static ImageApiVersion fromApiComplianceLevelString(String imageLevelString) {
    IMAGE_API_VERSION imageApiVersionEnum = apiComplianceLevelToEnum(imageLevelString);
    if (imageApiVersionEnum == null) {
      throw new IllegalArgumentException(imageLevelString + " is not a supported IIIF Image API Compliance level");
    }
    return new ImageApiVersion(imageApiVersionEnum);
  }

  /**
   * Converts numeric version number string into an enum item
   */
  private static IMAGE_API_VERSION numericStringToEnum(String input) {
    switch (input) {
      case IMAGE_API_VERSION_2_1_1_NUMERIC:
        return IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE;
      case IMAGE_API_VERSION_3_0_NUMERIC:
      case "3.0.0":
        return IMAGE_API_VERSION.THREE_POINT_ZERO;
    }
    return null;
  }

  /**
   * Converts IIIF Image API level string into an enum item
   */
  private static IMAGE_API_VERSION apiComplianceLevelToEnum(String input) {
    switch (input) {
      case IMAGE_API_VERSION_2_1_1_COMPLIANCE_LEVEL_2:
        return IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE;
      case IMAGE_API_VERSION_3_0_COMPLIANCE_LEVEL_1:
        return IMAGE_API_VERSION.THREE_POINT_ZERO;
    }
    return null;
  }

  /**
   * Returns the numeric string of this object's version
   */
  public String toNumericString() {
    switch (this.version) {
      case TWO_POINT_ONE_POINT_ONE:
        return IMAGE_API_VERSION_2_1_1_NUMERIC;
      case THREE_POINT_ZERO:
        return IMAGE_API_VERSION_3_0_NUMERIC;
    }
    return null;
  }

  /**
   * Checks whether two {@link ImageApiVersion} represent the same {@link IMAGE_API_VERSION}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ImageApiVersion) {
      return this.version.equals(((ImageApiVersion) obj).version);
    } else {
      return super.equals(obj);
    }
  }

  public IMAGE_API_VERSION getVersion() {
    return this.version;
  }

  /**
   * Enum to hold the various Image Api specification versions supported by the builder
   */
  public enum IMAGE_API_VERSION {
    TWO_POINT_ONE_POINT_ONE, THREE_POINT_ZERO
  }
}
