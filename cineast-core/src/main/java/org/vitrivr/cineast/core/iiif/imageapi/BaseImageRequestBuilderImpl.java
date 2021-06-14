package org.vitrivr.cineast.core.iiif.imageapi;

import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageRequestBuilder_v2.SIZE_MAX;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageRequestBuilder_v2.SIZE_PERCENTAGE;

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

  public static boolean isImageDimenValidFloat(Float quantity) {
    return quantity != null && !quantity.isNaN() && !quantity.isInfinite();
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

  public BaseImageRequestBuilderImpl setSizeScaledExact(Float width, Float height) {
    boolean isWidthValid = isImageDimenValidFloat(width);
    boolean isHeightValid = isImageDimenValidFloat(height);
    StringBuilder sizeString = new StringBuilder();
    if (isWidthValid) {
      sizeString.append(toSimplifiedFloatString(width));
    }
    sizeString.append(",");
    if (isHeightValid) {
      sizeString.append(toSimplifiedFloatString(height));
    }
    this.size = sizeString.toString();
    return this;
  }

  public BaseImageRequestBuilderImpl setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) {
    // If both width and height cannot be overridden by the server then it is the same case as exact scaling.
    if (!isWidthOverridable && !isHeightOverridable) {
      return setSizeScaledExact(width, height);
    }
    StringBuilder sizeString = new StringBuilder();
    if (isWidthOverridable) {
      sizeString.append("!");
    }
    sizeString.append(toSimplifiedFloatString(width));
    sizeString.append(",");
    if (isHeightOverridable) {
      sizeString.append("!");
    }
    sizeString.append(toSimplifiedFloatString(height));
    this.size = sizeString.toString();
    return this;
  }

  public BaseImageRequestBuilderImpl setSizePercentage(float n) {
    this.size = SIZE_PERCENTAGE + n;
    return this;
  }

  public BaseImageRequestBuilderImpl setRotation(float degree, boolean mirror) {
    this.rotation = (mirror ? "!" : "") + toSimplifiedFloatString(degree);
    return this;
  }

  public BaseImageRequestBuilderImpl setQuality(String quality) {
    this.quality = quality;
    return this;
  }

  public BaseImageRequestBuilderImpl setFormat(String format) {
    this.extension = format;
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
