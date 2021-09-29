package org.vitrivr.cineast.core.iiif.imageapi.v3;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_JPG;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_PNG;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_COLOR;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_DEFAULT;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_GRAY;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_BASE_URI_REDIRECT;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_CORS;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_JSONLD_MEDIA_TYPE;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_REGION_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_REGION_BY_PX;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_REGION_SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_ROTATION_BY_90s;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_CONFINED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_H;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_W;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_WH;

/**
 * Class to check for IIIF Image API Compliance Level specific support for features, quality and image formats
 */
public class ImageApiCompliance_v3 {

  public static String LEVEL_0 = "level0";
  public static String LEVEL_1 = "level1";
  public static String LEVEL_2 = "level2";

  public static boolean isFeatureSupported(String feature, String level) {
    if (level == null || level.isEmpty()) {
      return false;
    }
    switch (feature) {
      // Features supported by both level 1 and 2
      case SUPPORTS_REGION_BY_PX:
      case SUPPORTS_REGION_SQUARE:
      case SUPPORTS_SIZE_BY_W:
      case SUPPORTS_SIZE_BY_H:
      case SUPPORTS_SIZE_BY_WH:
        return level.equals(LEVEL_1) || level.equals(LEVEL_2);
      // Features only supported by level 2
      case SUPPORTS_REGION_BY_PCT:
      case SUPPORTS_SIZE_BY_PCT:
      case SUPPORTS_SIZE_BY_CONFINED_WH:
      case SUPPORTS_ROTATION_BY_90s:
        return level.equals(LEVEL_2);
    }
    return false;
  }

  public static boolean isQualitySupported(String quality, String level) {
    // All levels support the default quality
    if (quality.equals(QUALITY_DEFAULT)) {
      return true;
    }
    if (level.equals(LEVEL_2)) {
      return quality.equals(QUALITY_COLOR) || quality.equals(QUALITY_GRAY);
    }
    return false;
  }

  public static boolean isFormatSupported(String format, String level) {
    switch (format) {
      case EXTENSION_JPG:
        // All levels support the jpg format
        return true;
      case EXTENSION_PNG:
        return level.equals(LEVEL_2);
    }
    return false;
  }

  public static boolean isHttpFeatureSupported(String httpFeature, String level) {
    if (httpFeature.equals(SUPPORTS_BASE_URI_REDIRECT) || httpFeature.equals(SUPPORTS_CORS) || httpFeature.equals(SUPPORTS_JSONLD_MEDIA_TYPE)) {
      return level.equals(LEVEL_1) || level.equals(LEVEL_2);
    }
    return false;
  }

}
