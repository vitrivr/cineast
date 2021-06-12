package org.vitrivr.cineast.core.iiif.imageapi.v2_1_1;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl.isImageDimenValidFloat;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_REGION_SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_ABOVE_FULL;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_CONFINED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_DISTORTED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_H;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_W;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_WH;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestValidators;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation;
import org.vitrivr.cineast.core.iiif.imageapi.ImageRequest;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 09.06.21
 */
public class ImageRequestBuilder_v2_1_1_Impl implements ImageRequestBuilder_v2_1_1 {

  private static final Logger LOGGER = LogManager.getLogger();
  private final BaseImageRequestBuilder baseBuilder;
  private String size;
  private Validators validators;

  public ImageRequestBuilder_v2_1_1_Impl(String baseUrl) {
    this.baseBuilder = new BaseImageRequestBuilderImpl(baseUrl);
  }

  public ImageRequestBuilder_v2_1_1_Impl(ImageInformation imageInformation) {
    this(imageInformation.getAtId());
    this.validators = new Validators(imageInformation);
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionFull() {
    baseBuilder.setRegionFull();
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionSquare() throws IllegalArgumentException {
    if (validators != null) {
      validators.validateServerSupportsFeature(SUPPORTS_REGION_SQUARE, "Server does not support explicitly requesting square regions of images");
    }
    baseBuilder.setRegionSquare();
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionAbsolute(float x, float y, float w, float h) throws IllegalArgumentException {
    if (validators != null) {
      validators.validateServerSupportsRegionAbsolute(w, h);
    }
    baseBuilder.setRegionAbsolute(x, y, w, h);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionPercentage(float x, float y, float w, float h) throws IllegalArgumentException {
    if (validators != null) {
      validators.validateServerSupportsRegionPercentage(x, y, w, h);
    }
    baseBuilder.setRegionPercentage(x, y, w, h);
    return this;
  }

  public ImageRequestBuilder_v2_1_1_Impl setSizeFull() {
    this.size = SIZE_FULL;
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setSizeMax() {
    baseBuilder.setSizeMax();
    return this;
  }

  public ImageRequestBuilder_v2_1_1_Impl setSizeScaledExact(Float width, Float height) throws IllegalArgumentException {
    boolean isWidthValid = isImageDimenValidFloat(width);
    boolean isHeightValid = isImageDimenValidFloat(height);
    // Behaviour of server when neither width or height are provided is undefined. Thus, user should be forced to some other method such as setSizeMax.
    if (!isWidthValid && !isHeightValid) {
      throw new IllegalArgumentException("Either width or height must be a valid float value!");
    }
    if (validators != null) {
      validators.validateSizeScaledExact(width, height, isWidthValid, isHeightValid);
    }
    baseBuilder.setSizeScaledExact(width, height);
    return this;
  }

  public ImageRequestBuilder_v2_1_1_Impl setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    if (validators != null) {
      validators.validateSizeScaledBestFit(width, height, isWidthOverridable, isHeightOverridable);
    }
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
  public ImageRequestBuilder_v2_1_1_Impl setSizePercentage(float n) throws IllegalArgumentException {
    if (validators != null) {
      validators.validateSizePercentage(n);
    }
    baseBuilder.setSizePercentage(n);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRotation(float degree, boolean mirror) throws IllegalArgumentException {
    if (validators != null) {
      validators.validateSetRotation(degree, mirror);
    }
    if (degree < 0 || degree > 360) {
      throw new IllegalArgumentException("Rotation value can only be between 0° and 360°!");
    }
    baseBuilder.setRotation(degree, mirror);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setQuality(String quality) throws IllegalArgumentException {
    if (validators != null) {
      validators.validateServerSupportsQuality(quality);
    }
    baseBuilder.setQuality(quality);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setFormat(String format) throws IllegalArgumentException {
    if (validators != null) {
      validators.validateServerSupportsFormat(format);
    }
    baseBuilder.setFormat(format);
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

  private static class Validators extends BaseImageRequestValidators {

    private final ImageInformation imageInformation;

    public Validators(ImageInformation imageInformation) {
      super(imageInformation);
      this.imageInformation = imageInformation;
    }

    private void validateSizeScaledExact(Float width, Float height, boolean isWidthValid, boolean isHeightValid) {
      if (!isWidthValid) {
        validateServerSupportsFeature(SUPPORTS_SIZE_BY_H, "Server does not support requesting for image sizes by height alone");
      }
      if (!isHeightValid) {
        validateServerSupportsFeature(SUPPORTS_SIZE_BY_W, "Server does not support requesting for image sizes by width alone");
      }
      if (width > imageInformation.getWidth() || height > imageInformation.getHeight()) {
        validateServerSupportsFeature(SUPPORTS_SIZE_ABOVE_FULL, "Server does not support requesting for image sizes about the image's full size");
      }
      float requestAspectRatio = width / height;
      float originalAspectRatio = (float) imageInformation.getWidth() / imageInformation.getHeight();
      if (requestAspectRatio != originalAspectRatio) {
        validateServerSupportsFeature(SUPPORTS_SIZE_BY_DISTORTED_WH, "Server does not support requesting for image sizes that would distort the image");
      } else {
        validateServerSupportsFeature(SUPPORTS_SIZE_BY_WH, "Server does not support requesting for image sizes using width and height parameters");
      }
    }

    public boolean validateSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) {
      if (width > imageInformation.getWidth() || height > imageInformation.getHeight()) {
        validateServerSupportsFeature(SUPPORTS_SIZE_ABOVE_FULL, "Server does not support requesting for image sizes about the image's full size");
      }
      if (isWidthOverridable || isHeightOverridable) {
        validateServerSupportsFeature(SUPPORTS_SIZE_BY_CONFINED_WH, "Server does not support requesting images with overridable width or height parameters");
      }
      return false;
    }

    public void validateSizePercentage(float n) {
      validateServerSupportsFeature(SUPPORTS_SIZE_BY_PCT, "Server does not support requesting for image size using percentages");
      if (n > 100) {
        validateServerSupportsFeature(SUPPORTS_SIZE_ABOVE_FULL, "Server does not support requesting for image sizes about the image's full size");
      }
      if (n <= 0) {
        throw new IllegalArgumentException("Percentage value has to be greater than 0");
      }
    }
  }
}
