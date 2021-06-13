package org.vitrivr.cineast.core.iiif.imageapi.v2_1_1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_TIF;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_BITONAL;
import static org.vitrivr.cineast.core.iiif.imageapi.v2_1_1.ImageRequestBuilder_v2_1_1.SIZE_FULL;
import static org.vitrivr.cineast.core.iiif.imageapi.v2_1_1.ImageRequestBuilder_v2_1_1.SIZE_MAX;

import javax.naming.OperationNotSupportedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation;
import org.vitrivr.cineast.core.iiif.imageapi.ImageRequest;

/**
 * Only input validation and functions specific to Image API 2.1.1 need to be tested
 *
 * @author singaltanmay
 * @version 1.0
 * @created 10.06.21
 */
public class ImageRequestBuilder_v2_1_1_Test {

  private static final String BASE_URL = "https://libimages.princeton.edu/loris/pudl0001/5138415/00000011.jp2";

  public ImageRequestBuilder_v2_1_1 builder;

  @BeforeEach
  public void setup() {
    ImageInformation imageInformation = mock(ImageInformation.class);
    when(imageInformation.getAtId()).thenReturn(BASE_URL);
    when(imageInformation.isFeatureSupported(any())).thenReturn(false);
    builder = new ImageRequestBuilder_v2_1_1_Impl(imageInformation);
  }

  /**
   * Unit tests for methods that set the region of the image
   */
  @Nested
  class setRegionTests {

    @DisplayName("setRegion(SQUARE) not supported by server test")
    @Test
    void setRegionSquare() {
      assertThrows(OperationNotSupportedException.class, () -> {
        builder.setRegionSquare();
      });
    }

    @DisplayName("setRegion(ABSOLUTE) zero width or height test")
    @Test
    void setRegionAbsoluteZeroWH() {
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionAbsolute(75, 78, 0, 12));
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionAbsolute(136, 56, 21, 0));
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionAbsolute(184, 34, 0, 0));
    }

    @DisplayName("setRegion(ABSOLUTE) not supported by server test")
    @Test
    void setRegionAbsoluteNotSupported() {
      assertThrows(OperationNotSupportedException.class, () -> builder.setRegionAbsolute(75, 78, 835, 132));
    }

    @DisplayName("setRegion(PERCENTAGE) out of bounds test")
    @Test
    void setRegionPercentageOutOfBounds() {
      // x and y should lie between 0 and 100
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(-1, 50, 34, 56));
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(50, -1, 34, 56));
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(100.01f, 50, 34, 56));
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(50, 100.01f, 34, 56));
      // If x or y == 100 then image is out of bounds
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(100, 50, 50, 50));
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(50, 100, 50, 50));
      // Height and width should be > 0 and <= 100
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(50, 50, 0, 50));
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(50, 50, 100.01f, 50));
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(50, 50, 50, 0));
      assertThrows(IllegalArgumentException.class, () -> builder.setRegionPercentage(50, 50, 50, 100.01f));
    }

    @DisplayName("setRegion(PERCENTAGE) not supported by server test")
    @Test
    void setRegionPercentageNotSupported() {
      assertThrows(OperationNotSupportedException.class, () -> builder.setRegionPercentage(50, 50, 50, 50));
    }
  }

  /**
   * Unit tests for methods that set the size of the image
   */
  @Nested
  class setSizeTests {

    @DisplayName("setSizeFull test")
    @Test
    void setSizeFull() {
      ImageRequest request = builder.setSizeFull().build();
      assertNotNull(request);
      assertEquals(SIZE_FULL, request.getSize());
    }

    @DisplayName("setSizeMax test")
    @Test
    void setSizeMax() {
      ImageRequest request = builder.setSizeMax().build();
      assertNotNull(request);
      assertEquals(SIZE_MAX, request.getSize());
    }

    @DisplayName("setSizePercentage less than 0 test")
    @Test
    void setSizePercentageLessThan0() {
      assertThrows(IllegalArgumentException.class, () -> builder.setSizePercentage(-1));
    }

    @DisplayName("setSizePercentage not supported by server test")
    @Test
    void setSizePercentageNotSupported() {
      assertThrows(OperationNotSupportedException.class, () -> builder.setSizePercentage(23.45f));
    }

    @DisplayName("setSizeScaledExact neither width not height test")
    @Test
    void setSizeScaledExactNeitherWidthNorHeight() {
      assertThrows(IllegalArgumentException.class, () -> {
        builder.setSizeScaledExact(null, null).build();
      });
    }

    @DisplayName("setSizeScaledExact not supported by server test")
    @Test
    void setSizeScaledExactNotSupported() {
      assertThrows(OperationNotSupportedException.class, () -> {
        builder.setSizeScaledExact(122f, 532f).build();
      });
    }

    @DisplayName("setSizeScaledBestFit both width and height are overridable")
    @Test
    void setSizeScaledBestFitBothOverridable() {
      float width = 37.40f;
      float height = 467.65f;
      assertThrows(IllegalArgumentException.class, () -> {
        builder.setSizeScaledBestFit(width, height, true, true).build();
      });
    }

    @DisplayName("setSizeScaledBestFit not supported by server")
    @Test
    void setSizeScaledBestFitNotSupported() {
      float width = 37.40f;
      float height = 467.65f;
      assertThrows(OperationNotSupportedException.class, () -> {
        builder.setSizeScaledBestFit(width, height, true, false).build();
      });
    }

  }

  /**
   * Unit tests for methods that set the rotation of the image
   */
  @Nested
  class setRotationTests {

    @DisplayName("setRotation with invalid rotation degrees test")
    @Test
    void setRotationWithInvalidRotation() {
      float rotation = -423.94f;
      assertThrows(IllegalArgumentException.class, () -> {
        builder.setRotation(rotation, true).build();
      });
    }
  }

  /**
   * Unit tests for methods that set the quality of the image
   */
  @Nested
  class setQualityTests {

    @DisplayName("setQuality not supported test")
    @Test
    void setQuality() {
      assertThrows(NullPointerException.class, () -> builder.setQuality(QUALITY_BITONAL));
    }
  }

  /**
   * Unit tests for methods that set the file extension of the image
   */
  @Nested
  class setExtensionTests {

    @DisplayName("setExtension not supported by server test")
    @Test
    void setExtension() {
      assertThrows(NullPointerException.class, () -> builder.setFormat(EXTENSION_TIF));
    }
  }

}
