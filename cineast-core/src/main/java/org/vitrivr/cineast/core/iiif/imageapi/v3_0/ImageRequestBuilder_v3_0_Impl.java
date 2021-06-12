package org.vitrivr.cineast.core.iiif.imageapi.v3_0;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_COLOR;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_REGION_SQUARE;

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
public class ImageRequestBuilder_v3_0_Impl implements ImageRequestBuilder_v3_0 {

  private static final Logger LOGGER = LogManager.getLogger();
  private final BaseImageRequestBuilder baseBuilder;
  private Validators validators;

  public ImageRequestBuilder_v3_0_Impl(String baseUrl) {
    this.baseBuilder = new BaseImageRequestBuilderImpl(baseUrl);
  }

  public ImageRequestBuilder_v3_0_Impl(ImageInformation imageInformation) {
    this(imageInformation.getAtId());
    validators = new Validators(imageInformation);
  }

  @Override
  public ImageRequestBuilder_v3_0 setRegionFull() {
    baseBuilder.setRegionFull();
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setRegionSquare() {
    if (validators != null) {
      validators.validateServerSupportsFeature(SUPPORTS_REGION_SQUARE, "Server does not support explicitly requesting square regions of images");
    }
    baseBuilder.setRegionSquare();
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setRegionAbsolute(float x, float y, float w, float h) {
    if (validators != null) {
      validators.validateServerSupportsRegionAbsolute(w, h);
    }
    baseBuilder.setRegionAbsolute(x, y, w, h);
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setRegionPercentage(float x, float y, float w, float h) {
    if (validators != null) {
      validators.validateServerSupportsRegionPercentage(x, y, w, h);
    }
    baseBuilder.setRegionPercentage(x, y, w, h);
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setSizeMaxUpscaled() {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setSizeMaxNotUpscaled() {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setSizeScaledExactUpscaled(Float w, Float h) throws IllegalArgumentException {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setSizeScaledExactNotUpscaled(Float w, Float h) throws IllegalArgumentException {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setSizeScaledBestFitNotUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setSizeScaledBestFitUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setSizePercentageNotUpscaled(float n) {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setSizePercentageUpscaled(float n) {
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setRotation(float degree, boolean mirror) {
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
  public ImageRequestBuilder_v3_0 setQuality(String quality) {
    if (validators != null) {
      validators.validateServerSupportsQuality(quality);
    }
    baseBuilder.setQuality(quality);
    return this;
  }

  @Override
  public ImageRequestBuilder_v3_0 setFormat(String format) {
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

    public Validators(ImageInformation imageInformation) {
      super(imageInformation);
      this.imageInformation = imageInformation;
    }

    @Override
    public void validateServerSupportsQuality(String quality) {
      // Server can return any quality for a color request but doesn't have to list the color quality in the profiles data
      if (quality.equals(QUALITY_COLOR)) {
        return;
      }
      super.validateServerSupportsQuality(quality);
    }
  }
}
