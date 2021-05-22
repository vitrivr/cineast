package org.vitrivr.cineast.core.iiif.imageapi;

public class ImageRequestBuilder {

    public static final String REGION_FULL = "full";

    public static final String REGION_SQUARE = "square";

    public static final String REGION_PERCENTAGE = "pct:";

    private final String BASE_URL = "https://libimages.princeton.edu/loris/pudl0001/5138415/00000011.jp2";

    private final IMAGE_API_VERSION apiVersion;

    private String region;

    public ImageRequestBuilder(IMAGE_API_VERSION apiVersion) {
        this.apiVersion = apiVersion;
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
        if (regionType == REGION.ABSOLUTE) {
            this.region = REGION_FULL;
        } else if (regionType == REGION.SQUARE) {
            this.region = REGION_SQUARE;
        }
        return this;
    }

    public ImageRequest build() {
        ImageRequest imageRequest = new ImageRequest();
        imageRequest.setBaseUrl(this.BASE_URL);
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
