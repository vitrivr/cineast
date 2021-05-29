package org.vitrivr.cineast.core.iiif.imageapi;

/**
 * Builder class to generate a single IIIF Image Api request
 *
 * @author singaltanmay
 * @version 1.0
 * @created 29.05.21
 */
public class ImageRequestBuilder {

  public static final String REGION_FULL = "full";
  public static final String REGION_SQUARE = "square";
  public static final String REGION_PERCENTAGE = "pct:";
  /**
   * Deprecation Warning : The size keyword full will be replaced in favor of max in version 3.0. Until that time, the w, syntax should be considered the canonical form of request for the max size, unless max is equivalent to full.
   */
  @Deprecated
  public static final String SIZE_FULL = "full";
  public static final String SIZE_MAX = "max";
  public static final String SIZE_PERCENTAGE = "pct:";

  private final IMAGE_API_VERSION apiVersion;
  private final String baseUrl;
  private String region;
  private String size;

  public ImageRequestBuilder(IMAGE_API_VERSION apiVersion, String baseUrl) {
    this.apiVersion = apiVersion;
    this.baseUrl = baseUrl != null ? baseUrl : "https://libimages.princeton.edu/loris/pudl0001/5138415/00000011.jp2";
  }

  /**
   * Simplifies the float string to the least amount of decimal points. Example: Converts "12.3400" to "12.34" and "3.00" to "3"
   */
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

  public ImageRequestBuilder setRegion(REGION regionType) {
    if (regionType == REGION.FULL) {
      this.region = REGION_FULL;
    } else if (regionType == REGION.SQUARE) {
      this.region = REGION_SQUARE;
    }
    return this;
  }

  public ImageRequestBuilder setRegion(REGION regionType, float x, float y, float w, float h) {
    String coordinates = toSimplifiedFloatString(x) + "," + toSimplifiedFloatString(y) + "," +
        toSimplifiedFloatString(w) + "," + toSimplifiedFloatString(h);
    if (regionType == REGION.ABSOLUTE) {
      this.region = coordinates;
    } else if (regionType == REGION.PERCENTAGE) {
      this.region = REGION_PERCENTAGE + coordinates;
    }
    return this;
  }

  /**
   * The image or region is not scaled, and is returned at its full size. Deprecation Warning : The size keyword full will be replaced in favor of max in version 3.0. Until that time, the w, syntax should be considered the canonical form of request for the max size, unless max is equivalent to full.
   */
  @Deprecated
  public ImageRequestBuilder setSizeFull() {
    this.size = SIZE_FULL;
    return this;
  }

  /**
   * The image or region is returned at the maximum size available, as indicated by maxWidth, maxHeight, maxArea in the profile description. This is the same as full if none of these properties are provided.
   */
  public ImageRequestBuilder setSizeMax() {
    this.size = SIZE_MAX;
    return this;
  }

  /**
   * The width and height of the returned image is scaled to n% of the width and height of the extracted region. The aspect ratio of the returned image is the same as that of the extracted region.
   */
  public ImageRequestBuilder setSizePercentage(float n) {
    this.size = SIZE_PERCENTAGE + n;
    return this;
  }

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   */
  public ImageRequestBuilder setSizeScaledExact(Float width, Float height) throws IllegalArgumentException {
    boolean isWidthValid = width != null && !width.isNaN() && !width.isInfinite();
    boolean isHeightValid = height != null && !height.isNaN() && !height.isInfinite();
    // Behaviour of server when neither width or height are provided is undefined. Thus, user should be forced to some other method such as setSizeMax.
    if (!isWidthValid && !isHeightValid) {
      throw new IllegalArgumentException("Either width or height must be a valid float value!");
    }
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

  /**
   * The image content is scaled for the best fit such that the resulting width and height are less than or equal to the requested width and height. The exact scaling may be determined by the service provider, based on characteristics including image quality and system performance. The dimensions of the returned image content are calculated to maintain the aspect ratio of the extracted region.
   */
  public ImageRequestBuilder setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    // If both width and height cannot be overridden by the server then it is the same case as exact scaling.
    if (!isWidthOverridable && !isHeightOverridable) {
      return setSizeScaledExact(width, height);
    }
    // Behaviour of server when both width and height are overridable is undefined. Thus, user should be forced to some other method such as setSizeMax.
    if (isWidthOverridable && isHeightOverridable) {
      throw new IllegalArgumentException("Both width and height cannot be overridable!");
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

  public ImageRequest build() {
    ImageRequest imageRequest = new ImageRequest();
    imageRequest.setBaseUrl(this.baseUrl);
    imageRequest.setRegion(this.region == null ? REGION_FULL : this.region);
    imageRequest.setSize(this.size == null ? SIZE_FULL : this.size);
    return imageRequest;
  }

  /**
   * Enum to hold the various Image Api specification versions supported by the builder
   */
  public enum IMAGE_API_VERSION {
    TWO_POINT_ONE_POINT_ONE
  }

  public enum REGION {
    FULL,
    SQUARE,
    ABSOLUTE,
    PERCENTAGE
  }
}
