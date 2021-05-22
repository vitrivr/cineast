package org.vitrivr.cineast.core.iiif.imageapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.REGION.ABSOLUTE;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.REGION.FULL;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.REGION.PERCENTAGE;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.REGION.SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.REGION_PERCENTAGE;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageRequestBuilder.toFloatString;

class ImageRequestBuilderTest {

    private static final String BASE_URL = "https://baseurl.com/imageapi";

    ImageRequestBuilder builder;

    @BeforeEach
    void setup() {
        builder = new ImageRequestBuilder(TWO_POINT_ONE_POINT_ONE, BASE_URL);
    }

    @DisplayName("setRegion(FULL) test")
    @Test
    void setRegionFull() {
        ImageRequest request = builder.setRegion(FULL).build();
        String s = request.getUrl();
        assertNotNull(s);
        assertEquals(BASE_URL + "/" + ImageRequestBuilder.REGION_FULL, s);
    }

    @DisplayName("setRegion(SQUARE) test")
    @Test
    void setRegionSquare() {
        ImageRequest request = builder.setRegion(SQUARE).build();
        String s = request.getUrl();
        assertNotNull(s);
        assertEquals(BASE_URL + "/" + ImageRequestBuilder.REGION_SQUARE, s);
    }

    @DisplayName("setRegion(ABSOLUTE) test")
    @Test
    void setRegionAbsolute() {
        final float x = 125f;
        final float y = 15f;
        final float w = 120f;
        final float h = 140f;
        ImageRequest request = builder.setRegion(ABSOLUTE, x, y, w, h).build();
        String s = request.getUrl();
        assertNotNull(s);
        String coordinates = toFloatString(x) + "," + toFloatString(y) + ","
                + toFloatString(w) + "," + toFloatString(h);
        assertEquals(BASE_URL + "/" + coordinates, s);
    }

    @DisplayName("setRegion(PERCENTAGE) test")
    @Test
    void setRegionPercentage() {
        final float x = 125f;
        final float y = 15f;
        final float w = 120f;
        final float h = 140f;
        ImageRequest request = builder.setRegion(PERCENTAGE, x, y, w, h).build();
        String s = request.getUrl();
        assertNotNull(s);
        String coordinates = toFloatString(x) + "," + toFloatString(y) + ","
                + toFloatString(w) + "," + toFloatString(h);
        assertEquals(BASE_URL + "/" + REGION_PERCENTAGE + coordinates, s);
    }

}