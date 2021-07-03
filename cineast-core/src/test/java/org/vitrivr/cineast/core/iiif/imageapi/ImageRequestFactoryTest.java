package org.vitrivr.cineast.core.iiif.imageapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.vitrivr.cineast.core.iiif.IIIFConfig;
import org.vitrivr.cineast.core.iiif.IIIFConfig.IIIFItem;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION;

/**
 * Unit testing of the methods in the class {@link ImageRequestFactory}
 *
 * @author singaltanmay
 * @version 1.0
 * @created 16.06.21
 */
public class ImageRequestFactoryTest {

  private final String BASE_URL = "https://example.com";
  private final String IDENTIFIER = "identifier";
  private final String JSON_RESPONSE_v2 = "{\"profile\": [\"http://iiif.io/api/image/2/level2.json\",{}], \"protocol\": \"http://iiif.io/api/image\", \"height\": 7200, \"width\": 5351, \"@context\": \"http://iiif.io/api/image/2/context.json\", \"@id\": \"" + BASE_URL + "\"}";
  private final String JSON_RESPONSE_v3 = "{\"@context\":\"http://iiif.io/api/image/3/context.json\",\"id\":\"" + BASE_URL + "\",\"type\":\"ImageService3\",\"protocol\":\"http://iiif.io/api/image\",\"profile\":\"level2\",\"width\":1920,\"height\":1080}";
  private ImageRequestFactory factory;
  private MockedStatic<ImageInformationRequest> informationRequestMockedStatic;

  @BeforeEach
  void setup() {
    IIIFConfig iiifConfig = mock(IIIFConfig.class);
    when(iiifConfig.getBaseUrl()).thenReturn(BASE_URL);
    IIIFItem iiifItem = new IIIFItem();
    iiifItem.setIdentifier(IDENTIFIER);
    when(iiifConfig.getIiifItems()).thenReturn(Collections.singletonList(iiifItem));
    informationRequestMockedStatic = mockStatic(ImageInformationRequest.class);
    factory = new ImageRequestFactory(iiifConfig);
  }

  @AfterEach
  void tearDown() {
    informationRequestMockedStatic.close();
  }

  /**
   * Tests Image API version auto-detection when the server responds with an Image API 2.1.1 response
   */
  @DisplayName("determineHighestSupportedApiVersion() test for Image API 2.1.1")
  @Test
  void determineHighestSupportedApiVersion_v2() {
    // Test specific setup
    String url = BASE_URL + "/" + IDENTIFIER;
    informationRequestMockedStatic.when(() -> ImageInformationRequest.fetchImageInformation(url)).thenReturn(JSON_RESPONSE_v2);
    ImageApiVersion imageApiVersion = factory.determineHighestSupportedApiVersion(url);
    assertNotNull(imageApiVersion);
    assertEquals(IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE, imageApiVersion.getVersion());
  }

  /**
   * Tests Image API version auto-detection when the server responds with an Image API 3.0 response
   */
  @DisplayName("determineHighestSupportedApiVersion() test for Image API 3.0")
  @Test
  void determineHighestSupportedApiVersion_v3() {
    // Test specific setup
    String url = BASE_URL + "/" + IDENTIFIER;
    informationRequestMockedStatic.when(() -> ImageInformationRequest.fetchImageInformation(url)).thenReturn(JSON_RESPONSE_v3);
    ImageApiVersion imageApiVersion = factory.determineHighestSupportedApiVersion(url);
    assertNotNull(imageApiVersion);
    assertEquals(IMAGE_API_VERSION.THREE_POINT_ZERO, imageApiVersion.getVersion());
  }

}
