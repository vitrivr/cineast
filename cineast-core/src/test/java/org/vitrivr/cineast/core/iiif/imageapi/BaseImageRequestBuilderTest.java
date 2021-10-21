package org.vitrivr.cineast.core.iiif.imageapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_TIF;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_BITONAL;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.REGION_FULL;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.REGION_PERCENTAGE;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.REGION_SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.SIZE_MAX;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.SIZE_PERCENTAGE;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.toSimplifiedFloatString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit testing of methods in {@link BaseImageRequestBuilder}
 */
public class BaseImageRequestBuilderTest {

  private static final String BASE_URL = "https://www.example.com";

  public BaseImageRequestBuilder builder;

  @BeforeEach
  public void setup() {
    builder = new BaseImageRequestBuilder(BASE_URL);
  }

  /**
   * Unit tests for methods that set the region of the image
   */
  @Nested
  class setRegionTests {

    @DisplayName("setRegion(FULL) test")
    @Test
    void setRegionFull() {
      ImageRequest request = builder.setRegionFull().build();
      assertNotNull(request);
      assertEquals(REGION_FULL, request.getRegion());
    }

    @DisplayName("setRegion(SQUARE) test")
    @Test
    void setRegionSquare() {
      ImageRequest request = builder.setRegionSquare().build();
      assertNotNull(request);
      assertEquals(REGION_SQUARE, request.getRegion());
    }

    @DisplayName("setRegion(ABSOLUTE) test")
    @Test
    void setRegionAbsolute() {
      final float x = 125f;
      final float y = 15f;
      final float w = 120f;
      final float h = 140f;
      ImageRequest request = builder.setRegionAbsolute(x, y, w, h).build();
      assertNotNull(request);
      String coordinates = toSimplifiedFloatString(x) + "," + toSimplifiedFloatString(y) + ","
          + toSimplifiedFloatString(w) + "," + toSimplifiedFloatString(h);
      assertEquals(coordinates, request.getRegion());
    }

    @DisplayName("setRegion(PERCENTAGE) test")
    @Test
    void setRegionPercentage() {
      final float x = 125f;
      final float y = 15f;
      final float w = 120f;
      final float h = 140f;
      ImageRequest request = builder.setRegionPercentage(x, y, w, h).build();
      assertNotNull(request);
      String coordinates = toSimplifiedFloatString(x) + "," + toSimplifiedFloatString(y) + ","
          + toSimplifiedFloatString(w) + "," + toSimplifiedFloatString(h);
      assertEquals(REGION_PERCENTAGE + coordinates, request.getRegion());
    }
  }

  /**
   * Unit tests for methods that set the size of the image
   */
  @Nested
  class setSizeTests {

    @DisplayName("setSizeMax test")
    @Test
    void setSizeMax() {
      ImageRequest request = builder.setSizeMax().build();
      assertNotNull(request);
      assertEquals(SIZE_MAX, request.getSize());
    }

    @DisplayName("setSizePercentage test")
    @Test
    void setSizePercentage() {
      float percentage = 37.40f;
      ImageRequest request = builder.setSizePercentage(percentage).build();
      assertNotNull(request);
      assertEquals(SIZE_PERCENTAGE + toSimplifiedFloatString(percentage), request.getSize());
    }

    @DisplayName("setSizeScaledExact only width test")
    @Test
    void setSizeScaledExactOnlyWidth() {
      float width = 37.40f;
      ImageRequest request = builder.setSizeScaledExact(width, null).build();
      assertNotNull(request);
      assertEquals(toSimplifiedFloatString(width) + ",", request.getSize());
    }

    @DisplayName("setSizeScaledExact only height test")
    @Test
    void setSizeScaledExactOnlyHeight() {
      float height = 467.65f;
      ImageRequest request = builder.setSizeScaledExact(null, height).build();
      assertNotNull(request);
      assertEquals("," + toSimplifiedFloatString(height), request.getSize());
    }

    @DisplayName("setSizeScaledExact both width and height test")
    @Test
    void setSizeScaledExactBothWidthAndHeight() {
      float width = 37.40f;
      float height = 467.65f;
      ImageRequest request = builder.setSizeScaledExact(width, height).build();
      assertNotNull(request);
      assertEquals(toSimplifiedFloatString(width) + "," + toSimplifiedFloatString(height), request.getSize());
    }

    @DisplayName("setSizeScaledExact neither width not height test")
    @Test
    void setSizeScaledExactNeitherWidthNorHeight() {
      ImageRequest request = builder.setSizeScaledExact(null, null).build();
      assertNotNull(request);
      assertEquals(",", request.getSize());
    }

    @DisplayName("setSizeScaledBestFit width overridable")
    @Test
    void setSizeScaledBestFitWidthOverridable() {
      float width = 37.40f;
      float height = 467.65f;
      ImageRequest request = builder.setSizeScaledBestFit(width, height, true, false).build();
      assertNotNull(request);
      assertEquals("!" + toSimplifiedFloatString(width) + "," + toSimplifiedFloatString(height), request.getSize());
    }

    @DisplayName("setSizeScaledBestFit height overridable")
    @Test
    void setSizeScaledBestFitHeightOverridable() {
      float width = 37.40f;
      float height = 467.65f;
      ImageRequest request = builder.setSizeScaledBestFit(width, height, false, true).build();
      assertNotNull(request);
      assertEquals(toSimplifiedFloatString(width) + "," + "!" + toSimplifiedFloatString(height), request.getSize());
    }

    @DisplayName("setSizeScaledBestFit neither width nor height are overridable")
    @Test
    void setSizeScaledBestFitNeitherOverridable() {
      float width = 37.40f;
      float height = 467.65f;
      ImageRequest request = builder.setSizeScaledBestFit(width, height, false, false).build();
      assertNotNull(request);
      assertEquals(toSimplifiedFloatString(width) + "," + toSimplifiedFloatString(height), request.getSize());
    }
  }

  /**
   * Unit tests for methods that set the rotation of the image
   */
  @Nested
  class setRotationTests {

    @DisplayName("setRotation no mirroring test")
    @Test
    void setRotationNoMirroring() {
      float rotation = 23.450f;
      ImageRequest request = builder.setRotation(rotation, false).build();
      assertNotNull(request);
      assertEquals(toSimplifiedFloatString(rotation), request.getRotation());
    }

    @DisplayName("setRotation with mirroring test")
    @Test
    void setRotationWithMirroring() {
      float rotation = 23.450f;
      ImageRequest request = builder.setRotation(rotation, true).build();
      assertNotNull(request);
      assertEquals("!" + toSimplifiedFloatString(rotation), request.getRotation());
    }

    @DisplayName("setRotation with negative rotation degrees test")
    @Test
    void setRotationWithNegativeRotation() {
      float rotation = -423.94f;
      ImageRequest request = builder.setRotation(rotation, true).build();
      assertNotNull(request);
      assertEquals("!" + toSimplifiedFloatString(rotation), request.getRotation());
    }
  }

  /**
   * Unit tests for methods that set the quality of the image
   */
  @Nested
  class setQualityTests {

    @DisplayName("setQuality test")
    @Test
    void setQuality() {
      ImageRequest request = builder.setQuality(QUALITY_BITONAL).build();
      assertNotNull(request);
      assertEquals(QUALITY_BITONAL, request.getQuality());
    }
  }

  /**
   * Unit tests for methods that set the file extension of the image
   */
  @Nested
  class setExtensionTests {

    @DisplayName("setExtension test")
    @Test
    void setExtension() {
      ImageRequest request = builder.setFormat(EXTENSION_TIF).build();
      assertNotNull(request);
      assertEquals(EXTENSION_TIF, request.getExtension());
    }
  }

}