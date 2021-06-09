package org.vitrivr.cineast.core.iiif.imageapi.v2_1_1;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl.toSimplifiedFloatString;

import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.IMAGE_API_VERSION;
import org.vitrivr.cineast.core.iiif.imageapi.ImageRequest;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 09.06.21
 */
class ImageRequestBuilder_v2_1_1_Impl implements ImageRequestBuilder_v2_1_1 {

  public static final String SIZE_PERCENTAGE = "pct:";

  private final IMAGE_API_VERSION apiVersion;
  private final BaseImageRequestBuilder baseBuilder;
  private ImageInformation imageInformation;
  private String size;

  public ImageRequestBuilder_v2_1_1_Impl(IMAGE_API_VERSION apiVersion, String baseUrl) {
    this.apiVersion = apiVersion;
    this.baseBuilder = new BaseImageRequestBuilderImpl(baseUrl);
  }

  public ImageRequestBuilder_v2_1_1_Impl(ImageInformation imageInformation) {
    this(imageInformation.getImageApiVersion(), imageInformation.getAtId());
  }

  public ImageRequestBuilder_v2_1_1_Impl setSizeFull() {
    this.size = SIZE_FULL;
    return this;
  }

  /**
   * The width and height of the returned image is scaled to n% of the width and height of the extracted region. The aspect ratio of the returned image is the same as that of the extracted region.
   *
   * @return this {@link ImageRequestBuilder_v2_1_1_Impl}
   */
  public ImageRequestBuilder_v2_1_1_Impl setSizePercentage(float n) {
    this.size = SIZE_PERCENTAGE + n;
    return this;
  }

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @return this {@link ImageRequestBuilder_v2_1_1_Impl}
   */
  public ImageRequestBuilder_v2_1_1_Impl setSizeScaledExact(Float width, Float height) throws IllegalArgumentException {
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
   * @return this {@link ImageRequestBuilder_v2_1_1_Impl}
   */
  public ImageRequestBuilder_v2_1_1_Impl setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
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

  @Override
  public BaseImageRequestBuilder setRegionFull() {
    baseBuilder.setRegionFull();
    return null;
  }

  @Override
  public BaseImageRequestBuilder setRegionSquare() {
    return null;
  }

  @Override
  public BaseImageRequestBuilder setRegionAbsolute(float x, float y, float w, float h) {
    return null;
  }

  @Override
  public BaseImageRequestBuilder setRegionPercentage(float x, float y, float w, float h) {
    return null;
  }

  @Override
  public BaseImageRequestBuilder setSizeMax() {
    return null;
  }

  @Override
  public BaseImageRequestBuilder setRotation(float degree, boolean mirror) {
    return null;
  }

  @Override
  public BaseImageRequestBuilder setQuality(String quality) {
    return null;
  }

  @Override
  public BaseImageRequestBuilder setExtension(String extension) {
    return null;
  }

  @Override
  public ImageRequest build() {
    return null;
  }
}
