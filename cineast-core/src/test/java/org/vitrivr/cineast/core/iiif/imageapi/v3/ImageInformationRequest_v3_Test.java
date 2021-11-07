package org.vitrivr.cineast.core.iiif.imageapi.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_GIF;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_TIF;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_BITONAL;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_COLOR;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_GRAY;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_MIRRORING;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_REGION_SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_ROTATION_ARBITRARY;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_UPSCALING;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.SizesItem;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.TilesItem;

/**
 * Tests the parsing of JSON response received for an "info.json" into an ImageInformation_v3 object
 */
public class ImageInformationRequest_v3_Test {

  private static final String atContext = "http://iiif.io/api/image/3/context.json";
  private static final String id = "http://localhost:8182/iiif/3/jim&pam.jpeg";
  private static final String type = "ImageService3";
  private static final String protocol = "http://iiif.io/api/image";
  private static final String profile = "level2";
  private static final long width = 500;
  private static final long height = 281;
  private static final long maxArea = 140500;
  private static final long size1width = 125;
  private static final long size1height = 70;
  private static final long size2width = 250;
  private static final long size2height = 141;
  private static final long size3width = 500;
  private static final long size3height = 281;
  private static final long tile1width = 500;
  private static final long tile1height = 281;
  private static final int scaleFactor1 = 1;
  private static final int scaleFactor2 = 2;
  private static final int scaleFactor4 = 4;
  private static final String JSON_RESPONSE =
      "{\"@context\":\"" + atContext + "\",\"id\":\"" + id + "\",\"type\":\"" + type + "\",\"protocol\":\"" +
          protocol + "\",\"profile\":\"" + profile + "\",\"width\":" + width + ",\"height\":" + height + ",\"maxArea\":" + maxArea +
          ",\"sizes\":[{\"width\":" + size1width + ",\"height\":" + size1height + "},"
          + "{\"width\":" + size2width + ",\"height\":" + size2height + "},"
          + "{\"width\":" + size3width + ",\"height\":" + size3height + "}],"
          + "\"tiles\":[{\"width\":" + tile1width + ",\"height\":" + tile1height + ","
          + "\"scaleFactors\":[" + scaleFactor1 + "," + scaleFactor2 + "," + scaleFactor4 + "]}],"
          + "\"extraQualities\":[\"" + QUALITY_BITONAL + "\",\"" + QUALITY_COLOR + "\",\"" + QUALITY_GRAY + "\"],"
          + "\"extraFormats\":[\"" + EXTENSION_TIF + "\",\"" + EXTENSION_GIF + "\"],"
          + "\"extraFeatures\":[\"" + SUPPORTS_MIRRORING + "\",\"" + SUPPORTS_REGION_SQUARE + "\",\"" + SUPPORTS_ROTATION_ARBITRARY + "\",\"" + SUPPORTS_SIZE_BY_PCT + "\",\"" + SUPPORTS_SIZE_UPSCALING + "\"]}";

  private ImageInformationRequest_v3 imageInformationRequest_v3;

  @BeforeEach
  void setup() {
    imageInformationRequest_v3 = Mockito.mock(ImageInformationRequest_v3.class);
    Mockito.when(imageInformationRequest_v3.parseImageInformation(JSON_RESPONSE)).thenCallRealMethod();
  }

  /**
   * Tests the parsing of JSON response received for an "info.json" into an ImageInformation_v3 object
   */
  @DisplayName("parseImageInformationJson test")
  @Test
  void parseImageInformationJson() {
    ImageInformation_v3 imageInformation = imageInformationRequest_v3.parseImageInformation(JSON_RESPONSE);
    assertNotNull(imageInformation);
    assertEquals(atContext, imageInformation.getAtContext());
    assertEquals(id, imageInformation.getId());
    assertEquals(type, imageInformation.getType());
    assertEquals(protocol, imageInformation.getProtocol());
    assertEquals(profile, imageInformation.getProfile());
    assertEquals(height, imageInformation.getHeight());
    assertEquals(width, imageInformation.getWidth());
    assertEquals(maxArea, imageInformation.getMaxArea());
    assertSizesValid(imageInformation);
    assertTilesValid(imageInformation);
    assertExtraQualitiesValid(imageInformation);
    assertExtraFormatsValid(imageInformation);
    assertExtraFeaturesValid(imageInformation);
  }

  private void assertSizesValid(ImageInformation_v3 imageInformation) {
    List<SizesItem> sizesItemList = imageInformation.getSizes();
    assertNotNull(sizesItemList);
    assertEquals(3, sizesItemList.size());
    List<SizesItem> sizesItemsTestList = new LinkedList<>();
    sizesItemsTestList.add(new SizesItem(size1width, size1height));
    sizesItemsTestList.add(new SizesItem(size2width, size2height));
    sizesItemsTestList.add(new SizesItem(size3width, size3height));
    assertEquals(sizesItemsTestList, sizesItemList);
  }

  private void assertTilesValid(ImageInformation_v3 imageInformation) {
    List<TilesItem> tilesItemList = imageInformation.getTiles();
    assertNotNull(tilesItemList);
    assertEquals(1, tilesItemList.size());
    TilesItem tilesItem = tilesItemList.get(0);
    assertEquals(tile1width, tilesItem.width);
    assertEquals(tile1height, tilesItem.height);
    Integer[] scaleFactorsList = {scaleFactor1, scaleFactor2, scaleFactor4};
    assertEquals(Arrays.asList(scaleFactorsList), tilesItem.scaleFactors);
  }

  private void assertExtraQualitiesValid(ImageInformation_v3 imageInformation) {
    List<String> extraQualities = imageInformation.getExtraQualities();
    assertNotNull(extraQualities);
    String[] extraQualitiesList = {QUALITY_BITONAL, QUALITY_COLOR, QUALITY_GRAY};
    assertEquals(Arrays.asList(extraQualitiesList), extraQualities);
  }

  private void assertExtraFormatsValid(ImageInformation_v3 imageInformation) {
    List<String> extraFormats = imageInformation.getExtraFormats();
    assertNotNull(extraFormats);
    String[] extraFormatsList = {EXTENSION_TIF, EXTENSION_GIF};
    assertEquals(Arrays.asList(extraFormatsList), extraFormats);
  }

  private void assertExtraFeaturesValid(ImageInformation_v3 imageInformation) {
    List<String> extraFeatures = imageInformation.getExtraFeatures();
    assertNotNull(extraFeatures);
    String[] extraFeaturesList = {SUPPORTS_MIRRORING, SUPPORTS_REGION_SQUARE, SUPPORTS_ROTATION_ARBITRARY, SUPPORTS_SIZE_BY_PCT, SUPPORTS_SIZE_UPSCALING};
    assertEquals(Arrays.asList(extraFeaturesList), extraFeatures);
  }

}
