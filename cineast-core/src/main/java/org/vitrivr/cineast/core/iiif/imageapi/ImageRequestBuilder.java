package org.vitrivr.cineast.core.iiif.imageapi;

public class ImageRequestBuilder {

    public static final String REGION_FULL = "full";

    public static final String REGION_SQUARE = "square";

    public static final String REGION_PERCENTAGE = "pct:";

    private final String baseUrl;

    private final IMAGE_API_VERSION apiVersion;

    private String region;

    public ImageRequestBuilder(IMAGE_API_VERSION apiVersion, String baseUrl) {
        this.apiVersion = apiVersion;
        this.baseUrl = baseUrl != null ? baseUrl : "https://libimages.princeton.edu/loris/pudl0001/5138415/00000011" +
                ".jp2";
    }

    public static String toFloatString(float value) {
        String strValue = "";
        if (value % 1 == 0) {
            int intValue = (int) value;
            strValue = Integer.toString(intValue);
        } else {
            strValue = Float.toString(value);
        }
        return strValue;
    }

    public ImageRequestBuilder setRegion(REGION regionType) {
        if (regionType == REGION.FULL) {
            this.region = REGION_FULL;
        } else if (regionType == REGION.SQUARE) {
            this.region = REGION_SQUARE;
        }
        return this;
    }

    public ImageRequestBuilder setRegion(REGION regionType, float x, float y, float w, float h) {
        String coordinates = toFloatString(x) + "," + toFloatString(y) + "," +
                toFloatString(w) + "," + toFloatString(h);
        if (regionType == REGION.ABSOLUTE) {
            this.region = coordinates;
        } else if (regionType == REGION.PERCENTAGE) {
            this.region = REGION_PERCENTAGE + coordinates;
        }
        return this;
    }

    public ImageRequest build() {
        ImageRequest imageRequest = new ImageRequest();
        imageRequest.setBaseUrl(this.baseUrl);
        imageRequest.setRegion(this.region);
        return imageRequest;
    }

    public enum IMAGE_API_VERSION {
        TWO_POINT_ONE_POINT_ONE,
        THREE_POINT_ZERO
    }

    public enum REGION {
        FULL,
        SQUARE,
        ABSOLUTE,
        PERCENTAGE
    }

}
