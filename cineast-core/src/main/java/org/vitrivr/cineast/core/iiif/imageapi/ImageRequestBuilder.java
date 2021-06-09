package org.vitrivr.cineast.core.iiif.imageapi;

import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.IMAGE_API_VERSION;

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

  private final IMAGE_API_VERSION apiVersion;
  private final String baseUrl;
  private ImageInformation imageInformation;
  private String region;
  private String size;
  private String rotation;
  private String quality;
  private String extension;

  public ImageRequestBuilder(IMAGE_API_VERSION apiVersion, String baseUrl) {
    this.apiVersion = apiVersion;
    this.baseUrl = baseUrl != null ? baseUrl : "https://libimages.princeton.edu/loris/pudl0001/5138415/00000011.jp2";
  }

  public ImageRequestBuilder(ImageInformation imageInformation) {
    this.imageInformation = imageInformation;
    if (imageInformation == null) {
      throw new IllegalArgumentException("ImageInformation cannot be null!");
    }
    this.baseUrl = this.imageInformation.getAtId();
    this.apiVersion = this.imageInformation.getImageApiVersion();
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

  /**
   * The complete image is returned, without any cropping.
   *
   * @return this {@link ImageRequestBuilder}
   */
  public ImageRequestBuilder setRegionFull() {
    this.region = REGION_FULL;
    return this;
  }

  /**
   * The region is defined as an area where the width and height are both equal to the length of the shorter dimension of the complete image. The region may be positioned anywhere in the longer dimension of the image content at the server’s discretion, and centered is often a reasonable default.
   *
   * @return this {@link ImageRequestBuilder}
   */
  public ImageRequestBuilder setRegionSquare() {
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
   * @return this {@link ImageRequestBuilder}
   */
  public ImageRequestBuilder setRegionAbsolute(float x, float y, float w, float h) {
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
   * @return this {@link ImageRequestBuilder}
   */
  public ImageRequestBuilder setRegionPercentage(float x, float y, float w, float h) {
    String coordinates = toSimplifiedFloatString(x) + "," + toSimplifiedFloatString(y) + "," +
        toSimplifiedFloatString(w) + "," + toSimplifiedFloatString(h);
    this.region = REGION_PERCENTAGE + coordinates;
    return this;
  }

  /**
   * The image or region is not scaled, and is returned at its full size. Deprecation Warning : The size keyword full will be replaced in favor of max in version 3.0. Until that time, the w, syntax should be considered the canonical form of request for the max size, unless max is equivalent to full.
   *
   * @return this {@link ImageRequestBuilder}
   */
  @Deprecated
  public ImageRequestBuilder setSizeFull() {
    this.size = SIZE_FULL;
    return this;
  }

  /**
   * The image or region is returned at the maximum size available, as indicated by maxWidth, maxHeight, maxArea in the profile description. This is the same as full if none of these properties are provided.
   *
   * @return this {@link ImageRequestBuilder}
   */
  public ImageRequestBuilder setSizeMax() {
    this.size = SIZE_MAX;
    return this;
  }

  /**
   * The width and height of the returned image is scaled to n% of the width and height of the extracted region. The aspect ratio of the returned image is the same as that of the extracted region.
   *
   * @return this {@link ImageRequestBuilder}
   */
  public ImageRequestBuilder setSizePercentage(float n) {
    this.size = SIZE_PERCENTAGE + n;
    return this;
  }

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @return this {@link ImageRequestBuilder}
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
   *
   * @return this {@link ImageRequestBuilder}
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

  /**
   * This method is used to specify the mirroring and rotation applied to the image.
   *
   * @param degree Represents the number of degrees of clockwise rotation, and may be any floating point number from 0 to 360.
   * @param mirror Indicates if that the image should be mirrored by reflection on the vertical axis before any rotation is applied.
   * @return this {@link ImageRequestBuilder}
   */
  public ImageRequestBuilder setRotation(float degree, boolean mirror) {
    if (degree < 0 || degree > 360) {
      throw new IllegalArgumentException("Rotation value can only be between 0° and 360°!");
    }
    this.rotation = (mirror ? "!" : "") + toSimplifiedFloatString(degree);
    return this;
  }

  /**
   * This method is used to specify the quality of the image.
   *
   * @param quality The quality of the image
   * @return this {@link ImageRequestBuilder}
   */
  public ImageRequestBuilder setQuality(String quality) {
    this.quality = quality;
    return this;
  }

  /**
   * This method is used to specify the file extension of the image.
   *
   * @param extension The file extension of the image
   * @return this {@link ImageRequestBuilder}
   */
  public ImageRequestBuilder setExtension(String extension) {
    this.extension = extension;
    return this;
  }

  public ImageRequest build() {
    return new ImageRequest(
        this.baseUrl,
        this.region == null ? REGION_FULL : this.region,
        this.size == null ? SIZE_FULL : this.size,
        this.rotation == null ? "0" : this.rotation,
        this.quality == null ? QUALITY_DEFAULT : this.quality,
        this.extension == null ? EXTENSION_JPG : this.extension);
  }

}
