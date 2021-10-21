package org.vitrivr.cineast.core.iiif.imageapi.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.SizesItem;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.TilesItem;
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem;

/**
 * Tests the parsing of JSON response received for an "info.json" into an ImageInformation_v2 object
 */
class ImageInformationRequest_v2_Test {

  String JSON_RESPONSE = "{\"profile\": [\"http://iiif.io/api/image/2/level2.json\", {\"supports\": [\"canonicalLinkHeader\", \"profileLinkHeader\", \"mirroring\", \"rotationArbitrary\", \"regionSquare\", \"sizeAboveFull\"], \"qualities\": [\"default\", \"bitonal\", \"gray\", \"color\"], \"formats\": [\"jpg\", \"png\", \"gif\", \"webp\"]}], \"tiles\": [{\"width\": 1024, \"scaleFactors\": [1, 2, 4, 8, 16, 32]}], \"protocol\": \"http://iiif.io/api/image\", \"sizes\": [{\"width\": 168, \"height\": 225}, {\"width\": 335, \"height\": 450}, {\"width\": 669, \"height\": 900}, {\"width\": 1338, \"height\": 1800}, {\"width\": 2676, \"height\": 3600}, {\"width\": 5351, \"height\": 7200}], \"height\": 7200, \"width\": 5351, \"@context\": \"http://iiif.io/api/image/2/context.json\", \"@id\": \"https://libimages.princeton.edu/loris/pudl0001%2F5138415%2F00000011.jp2\"}";
  private ImageInformationRequest_v2 imageInformationRequest_v2;

  @BeforeEach
  void setup() {
    imageInformationRequest_v2 = Mockito.mock(ImageInformationRequest_v2.class);
    Mockito.when(imageInformationRequest_v2.parseImageInformation(JSON_RESPONSE)).thenCallRealMethod();
  }

  @DisplayName("parseImageInformationJson test")
  @Test
  void parseImageInformationJson() {
    ImageInformation_v2 imageInformation = imageInformationRequest_v2.parseImageInformation(JSON_RESPONSE);
    assertNotNull(imageInformation);
    assertEquals("http://iiif.io/api/image", imageInformation.getProtocol());
    assertEquals(7200, imageInformation.getHeight());
    assertEquals(5351, imageInformation.getWidth());
    assertEquals("http://iiif.io/api/image/2/context.json", imageInformation.getAtContext());
    assertEquals("https://libimages.princeton.edu/loris/pudl0001%2F5138415%2F00000011.jp2", imageInformation.getAtId());
    assertProfileValid(imageInformation);
    assertTilesValid(imageInformation);
    assertSizesValid(imageInformation);
  }

  private void assertProfileValid(ImageInformation_v2 imageInformation) {
    Pair<String, List<ProfileItem>> profile = imageInformation.getProfile();
    assertNotNull(profile);
    assertEquals("http://iiif.io/api/image/2/level2.json", profile.first);
    assertEquals(1, profile.second.size());
    ProfileItem profileItem = profile.second.get(0);
    String[] supportsList = {
        "canonicalLinkHeader",
        "profileLinkHeader",
        "mirroring",
        "rotationArbitrary",
        "regionSquare",
        "sizeAboveFull"
    };
    assertEquals(Arrays.asList(supportsList), profileItem.getSupports());
    String[] qualitiesList = {
        "default",
        "bitonal",
        "gray",
        "color"
    };
    assertEquals(Arrays.asList(qualitiesList), profileItem.getQualities());
    String[] formatsList = {
        "jpg",
        "png",
        "gif",
        "webp"
    };
    assertEquals(Arrays.asList(formatsList), profileItem.getFormats());
  }

  private void assertTilesValid(ImageInformation_v2 imageInformation) {
    List<TilesItem> tilesItemList = imageInformation.getTiles();
    assertNotNull(tilesItemList);
    assertEquals(1, tilesItemList.size());
    TilesItem tilesItem = tilesItemList.get(0);
    assertEquals(1024, tilesItem.width);
    Integer[] scaleFactorsList = {1, 2, 4, 8, 16, 32};
    assertEquals(Arrays.asList(scaleFactorsList), tilesItem.scaleFactors);
  }

  private void assertSizesValid(ImageInformation_v2 imageInformation) {
    List<SizesItem> sizesItemList = imageInformation.getSizes();
    assertNotNull(sizesItemList);
    assertEquals(6, sizesItemList.size());
    List<SizesItem> sizesItemsTestList = new LinkedList<>();
    sizesItemsTestList.add(new SizesItem(168, 225));
    sizesItemsTestList.add(new SizesItem(335, 450));
    sizesItemsTestList.add(new SizesItem(669, 900));
    sizesItemsTestList.add(new SizesItem(1338, 1800));
    sizesItemsTestList.add(new SizesItem(2676, 3600));
    sizesItemsTestList.add(new SizesItem(5351, 7200));
    assertEquals(sizesItemsTestList, sizesItemList);
  }
}