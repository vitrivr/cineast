package org.vitrivr.cineast.core.iiif.imageapi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.EXTENSION_JPG;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.QUALITY_DEFAULT;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.REGION_FULL;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.SIZE_FULL;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 03.06.21
 */
public class ImageRequestTest {

  private static final String BASE_URL = "https://libimages.princeton.edu/loris/pudl0001/5138415/00000011.jp2";

  /**
   * Integration test that downloads am image from the internet and saves it to the filesystem. Disabled to prevent unnecessary image files from being created.
   */
  @Disabled
  @DisplayName("saveToFile test")
  @Test
  public void saveToFile() {
    ImageRequest imageRequest = new ImageRequest(BASE_URL, REGION_FULL, SIZE_FULL, "0", QUALITY_DEFAULT, EXTENSION_JPG);
    System.out.println(imageRequest.generateIIIFRequestUrl());
    /** File path needs to be manually configured based on platform */
    String filePath = null;
    assertDoesNotThrow(() -> imageRequest.saveToFile(filePath, "testdefault"));
  }

}
