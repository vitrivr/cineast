package org.vitrivr.cineast.core.iiif.imageapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.iiif.UnsupportedIIIFAPIException;

class ImageRequestTest {

  private static final String IMAGE_URL = "https://ub-sipi.ub.unibas.ch/portraets/IBB_1_004853591.jpx/full/200,/0/default.jpg";

  @Test
  public void fromUrlTest() throws IOException, UnsupportedIIIFAPIException {
    var imageRequest = ImageRequest.fromUrl(IMAGE_URL);
    assertEquals(IMAGE_URL, imageRequest.generateIIIFRequestUrl());
  }
}