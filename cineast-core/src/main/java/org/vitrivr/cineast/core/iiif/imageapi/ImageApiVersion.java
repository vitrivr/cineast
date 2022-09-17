package org.vitrivr.cineast.core.iiif.imageapi;

/**
 * Utility class to hold the Image API version along with some helpful methods for converting into different formats
 *
 * @param version Variable to hold the Image API version of this object
 */
public record ImageApiVersion(IMAGE_API_VERSION version) {

  public static final String IMAGE_API_VERSION_2_1_1_COMPLIANCE_LEVEL_2 = "http://iiif.io/api/image/2/level2.json";
  public static final String IMAGE_API_VERSION_2_1_1_NUMERIC = "2.1.1";
  public static final String IMAGE_API_VERSION_3_0_COMPLIANCE_LEVEL_1 = "http://iiif.io/api/image/3/level1.json";
  public static final String IMAGE_API_VERSION_3_0_NUMERIC = "3.0";

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
    return switch (input) {
      case IMAGE_API_VERSION_2_1_1_NUMERIC -> IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE;
      case IMAGE_API_VERSION_3_0_NUMERIC, "3.0.0" -> IMAGE_API_VERSION.THREE_POINT_ZERO;
      default -> null;
    };
  }

  /**
   * Converts IIIF Image API level string into an enum item
   */
  private static IMAGE_API_VERSION apiComplianceLevelToEnum(String input) {
    return switch (input) {
      case IMAGE_API_VERSION_2_1_1_COMPLIANCE_LEVEL_2 -> IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE;
      case IMAGE_API_VERSION_3_0_COMPLIANCE_LEVEL_1 -> IMAGE_API_VERSION.THREE_POINT_ZERO;
      default -> null;
    };
  }

  /**
   * Returns the numeric string of this object's version
   */
  public String toNumericString() {
    return switch (this.version) {
      case TWO_POINT_ONE_POINT_ONE -> IMAGE_API_VERSION_2_1_1_NUMERIC;
      case THREE_POINT_ZERO -> IMAGE_API_VERSION_3_0_NUMERIC;
    };
  }

  /**
   * Checks whether two {@link ImageApiVersion} represent the same {@link IMAGE_API_VERSION}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ImageApiVersion) {
      return this.version.equals(((ImageApiVersion) obj).version);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return toNumericString().hashCode();
  }

  /**
   * Enum to hold the various Image Api specification versions supported by the builder
   */
  public enum IMAGE_API_VERSION {
    TWO_POINT_ONE_POINT_ONE, THREE_POINT_ZERO
  }
}
