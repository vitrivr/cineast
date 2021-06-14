package org.vitrivr.cineast.core.iiif.imageapi.v3;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_COLOR;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_REGION_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_REGION_SQUARE;

import javax.naming.OperationNotSupportedException;
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
 * @created 11.06.21
 */
public class ImageRequestBuilder_v3_Impl implements ImageRequestBuilder_v3 {

  private static final Logger LOGGER = LogManager.getLogger();
  private final BaseImageRequestBuilder baseBuilder;
  private Validators validators;

  public ImageRequestBuilder_v3_Impl(String baseUrl) {
    this.baseBuilder = new BaseImageRequestBuilderImpl(baseUrl);
  }

  public ImageRequestBuilder_v3_Impl(ImageInformation_v3 imageInformation) throws IllegalArgumentException {
    this(imageInformation.getId());
    validators = new Validators(imageInformation);
  }

  @Override
  public ImageRequestBuilder_v3 setRegionFull() {
    baseBuilder.setRegionFull();
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setRegionSquare() throws OperationNotSupportedException {
    if (validators != null) {
      validators.validateServerSupportsFeature(SUPPORTS_REGION_SQUARE, "Server does not support explicitly requesting square regions of images");
    }
    baseBuilder.setRegionSquare();
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setRegionAbsolute(float x, float y, float w, float h) throws OperationNotSupportedException {
    if (validators != null) {
      validators.validateServerSupportsRegionAbsolute(w, h);
    }
    baseBuilder.setRegionAbsolute(x, y, w, h);
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setRegionPercentage(float x, float y, float w, float h) throws OperationNotSupportedException {
    if (x < 0 || x > 100 || y < 0 || y > 100) {
      throw new IllegalArgumentException("Value should lie between 0 and 100");
    }
    if (x == 100 || y == 100) {
      throw new IllegalArgumentException("Request region is entirely outside the image's reported dimensional bounds");
    }
    if (w <= 0 || w > 100 || h <= 0 || h > 100) {
      throw new IllegalArgumentException("Height and width of the image must belong in the range (0, 100]");
    }
    if (validators != null) {
      validators.validateServerSupportsFeature(SUPPORTS_REGION_BY_PCT, "Server does not support requests for regions of images by percentage.");
    }
    baseBuilder.setRegionPercentage(x, y, w, h);
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setSizeMaxUpscaled() {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setSizeMaxNotUpscaled() {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setSizeScaledExactUpscaled(Float w, Float h) throws IllegalArgumentException {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setSizeScaledExactNotUpscaled(Float w, Float h) throws IllegalArgumentException {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setSizeScaledBestFitNotUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setSizeScaledBestFitUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setSizePercentageNotUpscaled(float n) {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setSizePercentageUpscaled(float n) {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setRotation(float degree, boolean mirror) throws OperationNotSupportedException {
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
  public ImageRequestBuilder_v3 setQuality(String quality) throws OperationNotSupportedException {
    if (validators != null) {
      validators.validateServerSupportsQuality(quality);
    }
    baseBuilder.setQuality(quality);
    return this;
  }

  @Override
  public ImageRequestBuilder_v3 setFormat(String format) throws OperationNotSupportedException {
    if (validators != null) {
      validators.validateServerSupportsFormat(format);
    }
    baseBuilder.setFormat(format);
    return this;
  }

  @Override
  public ImageRequest build() {
    return baseBuilder.build();
  }

  private static class Validators extends BaseImageRequestValidators {

    private final ImageInformation imageInformation;

    public Validators(ImageInformation imageInformation) throws IllegalArgumentException {
      super(imageInformation);
      this.imageInformation = imageInformation;
    }

    @Override
    public void validateServerSupportsQuality(String quality) throws OperationNotSupportedException {
      // Server can return any quality for a color request but doesn't have to list the color quality in the profiles data
      if (quality.equals(QUALITY_COLOR)) {
        return;
      }
      super.validateServerSupportsQuality(quality);
    }
  }
}
