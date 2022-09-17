package org.vitrivr.cineast.core.iiif.presentationapi;

import org.vitrivr.cineast.core.iiif.UnsupportedIIIFAPIException;

public record PresentationApiVersion(PRESENTATION_API_VERSION version) {

  public static final String PRESENTATION_API_VERSION_2_1_1_COMPLIANCE_LEVEL_2 = "http://iiif.io/api/presentation/2/context.json";
  public static final String PRESENTATION_API_VERSION_2_1_1_NUMERIC = "2.1.1";
  public static final String PRESENTATION_API_VERSION_3_0_COMPLIANCE_LEVEL_1 = "http://iiif.io/api/presentation/3/context.json";
  public static final String PRESENTATION_API_VERSION_3_0_NUMERIC = "3.0";

  public static PRESENTATION_API_VERSION parse(String complianceLevel) throws UnsupportedIIIFAPIException {
    return switch (complianceLevel) {
      case PRESENTATION_API_VERSION_2_1_1_COMPLIANCE_LEVEL_2 -> PRESENTATION_API_VERSION.TWO_POINT_ONE_POINT_ONE;
      case PRESENTATION_API_VERSION_3_0_COMPLIANCE_LEVEL_1 -> PRESENTATION_API_VERSION.THREE_POINT_ZERO;
      default -> throw new UnsupportedIIIFAPIException("Unknown IIIF Presentation API compliance level: " + complianceLevel);
    };
  }

  public enum PRESENTATION_API_VERSION {
    TWO_POINT_ONE_POINT_ONE, THREE_POINT_ZERO
  }
}
