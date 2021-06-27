package org.vitrivr.cineast.core.iiif.imageapi;

/**
 * Base builder class to generate a single IIIF Image API request
 *
 * @author singaltanmay
 * @version 1.0
 * @created 09.06.21
 */
public class BaseImageRequestBuilder {

  public static final String REGION_FULL = "full";
  public static final String REGION_SQUARE = "square";
  public static final String REGION_PERCENTAGE = "pct:";
  public static final String SIZE_MAX = "max";
  public static final String SIZE_PERCENTAGE = "pct:";
  public static final String QUALITY_COLOR = "color";
  public static final String QUALITY_GRAY = "gray";
  public static final String QUALITY_BITONAL = "bitonal";
  public static final String QUALITY_DEFAULT = "default";
  public static final String EXTENSION_JPG = "jpg";
  public static final String EXTENSION_TIF = "tif";
  public static final String EXTENSION_PNG = "png";
  public static final String EXTENSION_GIF = "gif";
  public static final String EXTENSION_JP2 = "jp2";
  public static final String EXTENSION_PDF = "pdf";
  public static final String EXTENSION_WEBP = "webp";

  private final String baseUrl;
  private String region;
  private String size;
  private String rotation;
  private String quality;
  private String extension;

  public BaseImageRequestBuilder(String baseUrl) {
    this.baseUrl = baseUrl;
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

  /**
   * The complete image is returned, without any cropping.
   *
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setRegionFull() {
    this.region = REGION_FULL;
    return this;
  }

  /**
   * The region is defined as an area where the width and height are both equal to the length of the shorter dimension of the complete image. The region may be positioned anywhere in the longer dimension of the image content at the server’s discretion, and centered is often a reasonable default.
   *
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setRegionSquare() {
    this.region = REGION_SQUARE;
    return this;
  }

  /**
   * The region of the full image to be returned is specified in terms of absolute pixel values.
   *
   * @param x Represents the number of pixels from the 0 position on the horizontal axis
   * @param y Represents the number of pixels from the 0 position on the vertical axis
   * @param w Represents the width of the region
   * @param h Represents the height of the region in pixels
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setRegionAbsolute(float x, float y, float w, float h) {
    this.region = toSimplifiedFloatString(x) + "," + toSimplifiedFloatString(y) + "," +
        toSimplifiedFloatString(w) + "," + toSimplifiedFloatString(h);
    return this;
  }

  /**
   * The region to be returned is specified as a sequence of percentages of the full image’s dimensions, as reported in the image information document.
   *
   * @param x Represents the number of pixels from the 0 position on the horizontal axis, calculated as a percentage of the reported width
   * @param y Represents the number of pixels from the 0 position on the vertical axis, calculated as a percentage of the reported height
   * @param w Represents the width of the region, calculated as a percentage of the reported width
   * @param h Represents the height of the region, calculated as a percentage of the reported height
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setRegionPercentage(float x, float y, float w, float h) {
    String coordinates = toSimplifiedFloatString(x) + "," + toSimplifiedFloatString(y) + "," +
        toSimplifiedFloatString(w) + "," + toSimplifiedFloatString(h);
    this.region = REGION_PERCENTAGE + coordinates;
    return this;
  }

  /**
   * The image or region is returned at the maximum size available, as indicated by maxWidth, maxHeight, maxArea in the profile description. This is the same as full if none of these properties are provided.
   *
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setSizeMax() {
    return setSizeMax(null);
  }

  /**
   * The image or region is returned at the maximum size available, as indicated by maxWidth, maxHeight, maxArea in the profile description. This is the same as full if none of these properties are provided.
   *
   * @param prefix Optional prefix added to the start of the string
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setSizeMax(String prefix) {
    this.size = (prefix != null ? prefix : "") + SIZE_MAX;
    return this;
  }

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setSizeScaledExact(Float width, Float height) {
    return setSizeScaledExact(width, height, null);
  }

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @param prefix Optional prefix added to the start of the string
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setSizeScaledExact(Float width, Float height, String prefix) {
    boolean isWidthValid = isImageDimenValidFloat(width);
    boolean isHeightValid = isImageDimenValidFloat(height);
    StringBuilder sizeString = new StringBuilder(prefix != null ? prefix : "");
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
   *
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) {
    return setSizeScaledBestFit(width, height, isWidthOverridable, isHeightOverridable, null);
  }

  /**
   * The image content is scaled for the best fit such that the resulting width and height are less than or equal to the requested width and height. The exact scaling may be determined by the service provider, based on characteristics including image quality and system performance. The dimensions of the returned image content are calculated to maintain the aspect ratio of the extracted region.
   *
   * @param prefix Optional prefix added to the start of the string
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable, String prefix) {
    // If both width and height cannot be overridden by the server then it is the same case as exact scaling.
    if (!isWidthOverridable && !isHeightOverridable) {
      return setSizeScaledExact(width, height);
    }
    StringBuilder sizeString = new StringBuilder(prefix != null ? prefix : "");
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

  /**
   * The width and height of the returned image is scaled to n% of the width and height of the extracted region. The aspect ratio of the returned image is the same as that of the extracted region.
   *
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setSizePercentage(float n) {
    return setSizePercentage(n, null);
  }

  /**
   * The width and height of the returned image is scaled to n% of the width and height of the extracted region. The aspect ratio of the returned image is the same as that of the extracted region.
   *
   * @param prefix Optional prefix added to the start of the string
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setSizePercentage(float n, String prefix) {
    this.size = (prefix != null ? prefix : "") + SIZE_PERCENTAGE + n;
    return this;
  }

  /**
   * This method is used to specify the mirroring and rotation applied to the image.
   *
   * @param degree Represents the number of degrees of clockwise rotation, and may be any floating point number from 0 to 360.
   * @param mirror Indicates if that the image should be mirrored by reflection on the vertical axis before any rotation is applied.
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setRotation(float degree, boolean mirror) {
    this.rotation = (mirror ? "!" : "") + toSimplifiedFloatString(degree);
    return this;
  }

  /**
   * This method is used to specify the quality of the image.
   *
   * @param quality The quality of the image
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setQuality(String quality) {
    this.quality = quality;
    return this;
  }

  /**
   * This method is used to specify the file format of the image.
   *
   * @param format The file format of the image
   * @return this {@link BaseImageRequestBuilder}
   */
  public BaseImageRequestBuilder setFormat(String format) {
    this.extension = format;
    return this;
  }

  /**
   * This method builds a new ImageRequest with the parameters set using the dedicated setter methods
   */
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
