package org.vitrivr.cineast.core.iiif.imageapi;

/**
 * Base builder class to generate a single IIIF Image Api request
 *
 * @author singaltanmay
 * @version 1.0
 * @created 09.06.21
 */
public class BaseImageRequestBuilderImpl implements BaseImageRequestBuilder {

  private final String baseUrl;
  private String region;
  private String size;
  private String rotation;
  private String quality;
  private String extension;

  public BaseImageRequestBuilderImpl(String baseUrl) {
    this.baseUrl = baseUrl != null ? baseUrl : "https://libimages.princeton.edu/loris/pudl0001/5138415/00000011.jp2";
  }

  public static String toSimplifiedFloatString(float value) {
    String strValue;
    if (value % 1 == 0) {
      int intValue = (int) value;
      strValue = Integer.toString(intValue);
    } else {
      strValue = Float.toString(value);
    }
    return strValue;
  }

  public BaseImageRequestBuilderImpl setRegionFull() {
    this.region = REGION_FULL;
    return this;
  }

  public BaseImageRequestBuilderImpl setRegionSquare() {
    this.region = REGION_SQUARE;
    return this;
  }

  public BaseImageRequestBuilderImpl setRegionAbsolute(float x, float y, float w, float h) {
    this.region = toSimplifiedFloatString(x) + "," + toSimplifiedFloatString(y) + "," +
        toSimplifiedFloatString(w) + "," + toSimplifiedFloatString(h);
    return this;
  }

  public BaseImageRequestBuilderImpl setRegionPercentage(float x, float y, float w, float h) {
    String coordinates = toSimplifiedFloatString(x) + "," + toSimplifiedFloatString(y) + "," +
        toSimplifiedFloatString(w) + "," + toSimplifiedFloatString(h);
    this.region = REGION_PERCENTAGE + coordinates;
    return this;
  }

  public BaseImageRequestBuilderImpl setSizeMax() {
    this.size = SIZE_MAX;
    return this;
  }

  public BaseImageRequestBuilderImpl setRotation(float degree, boolean mirror) {
    if (degree < 0 || degree > 360) {
      throw new IllegalArgumentException("Rotation value can only be between 0° and 360°!");
    }
    this.rotation = (mirror ? "!" : "") + toSimplifiedFloatString(degree);
    return this;
  }

  public BaseImageRequestBuilderImpl setQuality(String quality) {
    this.quality = quality;
    return this;
  }

  public BaseImageRequestBuilderImpl setExtension(String extension) {
    this.extension = extension;
    return this;
  }

  public ImageRequest build() {
    return new ImageRequest()
        .setBaseUrl(this.baseUrl)
        .setRegion(this.region)
        .setSize(this.size)
        .setRotation(this.rotation)
        .setQuality(this.quality)
        .setExtension(this.extension);
  }
}
