package org.vitrivr.cineast.core.iiif.imageapi.v2_1_1;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl.isImageDimenValidFloat;

import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation;
import org.vitrivr.cineast.core.iiif.imageapi.ImageRequest;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 09.06.21
 */
public class ImageRequestBuilder_v2_1_1_Impl implements ImageRequestBuilder_v2_1_1 {

  private final BaseImageRequestBuilder baseBuilder;
  private ImageInformation imageInformation;
  private String size;

  public ImageRequestBuilder_v2_1_1_Impl(String baseUrl) {
    this.baseBuilder = new BaseImageRequestBuilderImpl(baseUrl);
  }

  public ImageRequestBuilder_v2_1_1_Impl(ImageInformation imageInformation) {
    this(imageInformation.getAtId());
  }

  public ImageRequestBuilder_v2_1_1_Impl setSizeFull() {
    this.size = SIZE_FULL;
    return this;
  }

  public ImageRequestBuilder_v2_1_1_Impl setSizeScaledExact(Float width, Float height) throws IllegalArgumentException {
    // Behaviour of server when neither width or height are provided is undefined. Thus, user should be forced to some other method such as setSizeMax.
    if (!isImageDimenValidFloat(width) && !isImageDimenValidFloat(height)) {
      throw new IllegalArgumentException("Either width or height must be a valid float value!");
    }
    baseBuilder.setSizeScaledExact(width, height);
    return this;
  }

  public ImageRequestBuilder_v2_1_1_Impl setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    // If both width and height cannot be overridden by the server then it is the same case as exact scaling.
    if (!isWidthOverridable && !isHeightOverridable) {
      return this.setSizeScaledExact(width, height);
    }
    // Behaviour of server when both width and height are overridable is undefined. Thus, user should be forced to some other method such as setSizeMax.
    if (isWidthOverridable && isHeightOverridable) {
      throw new IllegalArgumentException("Both width and height cannot be overridable!");
    }
    baseBuilder.setSizeScaledBestFit(width, height, isWidthOverridable, isHeightOverridable);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionFull() {
    baseBuilder.setRegionFull();
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionSquare() {
    baseBuilder.setRegionSquare();
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionAbsolute(float x, float y, float w, float h) {
    baseBuilder.setRegionAbsolute(x, y, w, h);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionPercentage(float x, float y, float w, float h) {
    baseBuilder.setRegionPercentage(x, y, w, h);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setSizeMax() {
    baseBuilder.setSizeMax();
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setSizePercentage(float n) {
    baseBuilder.setSizePercentage(n);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRotation(float degree, boolean mirror) {
    if (degree < 0 || degree > 360) {
      throw new IllegalArgumentException("Rotation value can only be between 0° and 360°!");
    }
    baseBuilder.setRotation(degree, mirror);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setQuality(String quality) {
    baseBuilder.setQuality(quality);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setExtension(String extension) {
    baseBuilder.setExtension(extension);
    return this;
  }

  @Override
  public ImageRequest build() {
    ImageRequest imageRequest = baseBuilder.build();
    if (this.size != null && !this.size.isEmpty()) {
      imageRequest.setSize(this.size);
    }
    return imageRequest;
  }
}
