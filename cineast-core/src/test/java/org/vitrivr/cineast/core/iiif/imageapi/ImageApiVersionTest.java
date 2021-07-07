package org.vitrivr.cineast.core.iiif.imageapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION_2_1_1_COMPLIANCE_LEVEL_2;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION_2_1_1_NUMERIC;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION_3_0_COMPLIANCE_LEVEL_1;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION_3_0_NUMERIC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION;

/**
 * Unit testing of methods in class {@link ImageApiVersion}
 */
public class ImageApiVersionTest {

  private ImageApiVersion version2;
  private ImageApiVersion version3;

  @BeforeEach
  void setup() {
    version2 = new ImageApiVersion(IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE);
    version3 = new ImageApiVersion(IMAGE_API_VERSION.THREE_POINT_ZERO);
  }

  /**
   * Test creation of new object using the numeric string
   */
  @Test
  void fromNumericString() {
    assertEquals(version2, ImageApiVersion.fromNumericString(IMAGE_API_VERSION_2_1_1_NUMERIC));
    assertEquals(version3, ImageApiVersion.fromNumericString(IMAGE_API_VERSION_3_0_NUMERIC));
  }

  /**
   * Test creation of new object using the api compliance level string
   */
  @Test
  void fromApiComplianceLevelString() {
    assertEquals(version2, ImageApiVersion.fromApiComplianceLevelString(IMAGE_API_VERSION_2_1_1_COMPLIANCE_LEVEL_2));
    assertEquals(version3, ImageApiVersion.fromApiComplianceLevelString(IMAGE_API_VERSION_3_0_COMPLIANCE_LEVEL_1));
  }

  /**
   * Test conversion of numeric string to {@link IMAGE_API_VERSION} member
   */
  @Test
  void toNumericString() {
    assertEquals(IMAGE_API_VERSION_2_1_1_NUMERIC, version2.toNumericString());
    assertEquals(IMAGE_API_VERSION_3_0_NUMERIC, version3.toNumericString());
  }

}
