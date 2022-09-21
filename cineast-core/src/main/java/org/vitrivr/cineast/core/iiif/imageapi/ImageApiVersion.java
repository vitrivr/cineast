package org.vitrivr.cineast.core.iiif.imageapi;

import org.vitrivr.cineast.core.iiif.UnsupportedIIIFAPIException;
import org.vitrivr.cineast.core.iiif.presentationapi.PresentationApiVersion.PRESENTATION_API_VERSION;

/**
 * Utility class to hold the Image API version along with some helpful methods for converting into different formats
 *
 * @param version Variable to hold the Image API version of this object
 */
public record ImageApiVersion(IMAGE_API_VERSION version) {

  public static final String IMAGE_API_VERSION_2_1_1 = "http://iiif.io/api/image/2/context.json";
  public static final String IMAGE_API_VERSION_2_1_1_NUMERIC = "2.1.1";
  public static final String IMAGE_API_VERSION_3_0 = "http://iiif.io/api/image/3/context.json";
  public static final String IMAGE_API_VERSION_3_0_NUMERIC = "3.0";

  public static IMAGE_API_VERSION parse(String versionString) throws UnsupportedIIIFAPIException {
    return switch (versionString) {
      case IMAGE_API_VERSION_2_1_1 -> IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE;
      case IMAGE_API_VERSION_3_0 -> IMAGE_API_VERSION.THREE_POINT_ZERO;
      default -> throw new UnsupportedIIIFAPIException("Unknown IIIF Image API version: " + versionString);
    };
  }

  /**
   * Enum to hold the various Image Api specification versions supported by the builder
   */
  public enum IMAGE_API_VERSION {
    TWO_POINT_ONE_POINT_ONE, THREE_POINT_ZERO
  }
}
