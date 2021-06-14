package org.vitrivr.cineast.core.iiif.imageapi.v3;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.*;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.*;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 15.06.21
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
        // All levels support the jpg format
        if (format.equals(EXTENSION_JPG)) {
            return true;
        }
        if (format.equals(EXTENSION_PNG)) {
            return level.equals(LEVEL_2);
        }
        return false;
    }

}
