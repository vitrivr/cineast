package org.vitrivr.cineast.core.iiif.imageapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit testing of methods in class {@link ImageRequest}
 */
public class ImageRequestTest {

  /**
   * Tests the percent encoding of url path parameters
   */
  @Test
  void percentEncode() {
    String encoded = ImageRequest.percentEncode("https://example.com/?54#a%3C69:FT[@someone]");
    assertEquals("https:%2F%2Fexample.com%2F%3F54%23a%253C69:FT%5B%40someone%5D", encoded);
  }

  /**
   * Tests the order of the path parameters in the final url
   */
  @Test
  void generateIIIFRequestUrl() {
    ImageRequest imageRequest = new ImageRequest();
    imageRequest.setBaseUrl("https://example.com/test");
    imageRequest.setRegion("full");
    imageRequest.setSize("max");
    imageRequest.setRotation("0");
    imageRequest.setQuality("default");
    imageRequest.setExtension("jpg");
    String url = imageRequest.generateIIIFRequestUrl();
    assertEquals("https://example.com/test/full/max/0/default.jpg", url);
  }

}
