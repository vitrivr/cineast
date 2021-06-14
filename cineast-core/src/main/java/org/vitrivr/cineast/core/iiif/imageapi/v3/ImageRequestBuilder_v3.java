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
public class ImageRequestBuilder_v3 {

  public static final String SIZE_MAX_NOT_UPSCALED = "max";
  public static final String SIZE_MAX_UPSCALED = "^max";
  public static final String SIZE_PERCENTAGE_NOT_UPSCALED = "pct:";
  public static final String SIZE_PERCENTAGE_UPSCALED = "^pct:";
  private static final Logger LOGGER = LogManager.getLogger();
  private final BaseImageRequestBuilder baseBuilder;
  private Validators validators;

  public ImageRequestBuilder_v3(String baseUrl) {
    this.baseBuilder = new BaseImageRequestBuilderImpl(baseUrl);
  }

  public ImageRequestBuilder_v3(ImageInformation_v3 imageInformation) throws IllegalArgumentException {
    this(imageInformation.getId());
    validators = new Validators(imageInformation);
  }

  /**
   * The full image is returned, without any cropping.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setRegionFull() {
    baseBuilder.setRegionFull();
    return this;
  }

  /**
   * The region is defined as an area where the width and height are both equal to the length of the shorter dimension of the full image. The region may be positioned anywhere in the longer dimension of the full image at the server’s discretion, and centered is often a reasonable default.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setRegionSquare() throws OperationNotSupportedException {
    if (validators != null) {
      validators.validateServerSupportsFeature(SUPPORTS_REGION_SQUARE, "Server does not support explicitly requesting square regions of images");
    }
    baseBuilder.setRegionSquare();
    return this;
  }

  /**
   * The region of the full image to be returned is specified in terms of absolute pixel values.
   *
   * @param x Represents the number of pixels from the 0 position on the horizontal axis
   * @param y Represents the number of pixels from the 0 position on the vertical axis
   * @param w Represents the width of the region
   * @param h Represents the height of the region in pixels
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setRegionAbsolute(float x, float y, float w, float h) throws OperationNotSupportedException {
    if (validators != null) {
      validators.validateServerSupportsRegionAbsolute(w, h);
    }
    baseBuilder.setRegionAbsolute(x, y, w, h);
    return this;
  }

  /**
   * The region to be returned is specified as a sequence of percentages of the full image’s dimensions, as reported in the image information document.
   *
   * @param x Represents the number of pixels from the 0 position on the horizontal axis, calculated as a percentage of the reported width
   * @param y Represents the number of pixels from the 0 position on the vertical axis, calculated as a percentage of the reported height
   * @param w Represents the width of the region, calculated as a percentage of the reported width
   * @param h Represents the height of the region, calculated as a percentage of the reported height
   * @return this {@link ImageRequestBuilder_v3}
   */
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

  /**
   * The extracted region is scaled to the maximum size permitted by maxWidth, maxHeight, or maxArea. If the resulting dimensions are greater than the pixel width and height of the extracted region, the extracted region is upscaled.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setSizeMaxUpscaled() {
    return this;
  }

  /**
   * The extracted region is returned at the maximum size available, but will not be upscaled. The resulting image will have the pixel dimensions of the extracted region, unless it is constrained to a smaller size by maxWidth, maxHeight, or maxArea
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setSizeMaxNotUpscaled() {
    return this;
  }

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @param w The extracted region should be scaled so that the width of the returned image is exactly equal to w. If w is greater than the pixel width of the extracted region, the extracted region is upscaled.
   * @param h The extracted region should be scaled so that the height of the returned image is exactly equal to h. If h is greater than the pixel height of the extracted region, the extracted region is upscaled.
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException If both height and width are undefined then an IllegalArgumentException is thrown
   */
  public ImageRequestBuilder_v3 setSizeScaledExactUpscaled(Float w, Float h) throws IllegalArgumentException {
    return this;
  }

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @param w The extracted region should be scaled so that the width of the returned image is exactly equal to w. The value of w must not be greater than the width of the extracted region.
   * @param h The extracted region should be scaled so that the height of the returned image is exactly equal to h. The value of h must not be greater than the height of the extracted region.
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException If both height and width are undefined or greater than maxHeight or maxWidth then an IllegalArgumentException is thrown
   */
  public ImageRequestBuilder_v3 setSizeScaledExactNotUpscaled(Float w, Float h) throws IllegalArgumentException {
    return this;
  }

  /**
   * The extracted region is scaled so that the width and height of the returned image are not greater than w and h, while maintaining the aspect ratio. The returned image must be as large as possible but not larger than the extracted region, w or h, or server-imposed limits.
   *
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException Behaviour of server when both width and height are overridable is undefined
   */
  public ImageRequestBuilder_v3 setSizeScaledBestFitNotUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    return this;
  }

  /**
   * The extracted region is scaled so that the width and height of the returned image are not greater than w and h, while maintaining the aspect ratio. The returned image must be as large as possible but not larger than w, h, or server-imposed limits.
   *
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException Behaviour of server when both width and height are overridable is undefined
   */
  public ImageRequestBuilder_v3 setSizeScaledBestFitUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    return this;
  }

  /**
   * The width and height of the returned image is scaled to n percent of the width and height of the extracted region. The value of n must not be greater than 100.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setSizePercentageNotUpscaled(float n) {
    return this;
  }

  /**
   * The width and height of the returned image is scaled to n percent of the width and height of the extracted region. For values of n greater than 100, the extracted region is upscaled.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setSizePercentageUpscaled(float n) {
    return this;
  }

  /**
   * This method is used to specify the mirroring and rotation applied to the image.
   *
   * @param degree Represents the number of degrees of clockwise rotation, and may be any floating point number from 0 to 360.
   * @param mirror Indicates if that the image should be mirrored by reflection on the vertical axis before any rotation is applied.
   * @return this {@link ImageRequestBuilder_v3}
   */
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

  /**
   * This method is used to specify the quality of the image.
   *
   * @param quality The quality of the image
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setQuality(String quality) throws OperationNotSupportedException {
    if (validators != null) {
      validators.validateServerSupportsQuality(quality);
    }
    baseBuilder.setQuality(quality);
    return this;
  }

  /**
   * This method is used to specify the file extension of the image.
   *
   * @param format The file extension of the image
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setFormat(String format) throws OperationNotSupportedException {
    if (validators != null) {
      validators.validateServerSupportsFormat(format);
    }
    baseBuilder.setFormat(format);
    return this;
  }

  /** This method builds a new ImageRequest with the parameters set using the dedicated setter methods */
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
