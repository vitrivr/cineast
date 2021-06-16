package org.vitrivr.cineast.core.iiif.imageapi.v3;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_COLOR;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.isImageDimenValidFloat;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_REGION_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_REGION_SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_CONFINED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_H;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_W;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_BY_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3.SUPPORTS_SIZE_UPSCALING;

import javax.naming.OperationNotSupportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestValidators;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation;
import org.vitrivr.cineast.core.iiif.imageapi.ImageRequest;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 11.06.21
 */
public class ImageRequestBuilder_v3 {

  public static final String PREFIX_UPSCALING_MODIFIER = "^";
  private static final Logger LOGGER = LogManager.getLogger();
  private final BaseImageRequestBuilder baseBuilder;
  private Validators validators;
  private String size;

  public ImageRequestBuilder_v3(String baseUrl) {
    this.baseBuilder = new BaseImageRequestBuilder(baseUrl);
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
    BaseImageRequestValidators.validateWidthAndHeightGreaterThanZero(w, h);
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
    BaseImageRequestValidators.validatePercentageBoundsValid(x, y, w, h);
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
  public ImageRequestBuilder_v3 setSizeMaxUpscaled() throws OperationNotSupportedException {
    if (validators != null) {
      validators.validateSizeMax(true);
    }
    baseBuilder.setSizeMax(PREFIX_UPSCALING_MODIFIER);
    return this;
  }

  /**
   * The extracted region is returned at the maximum size available, but will not be upscaled. The resulting image will have the pixel dimensions of the extracted region, unless it is constrained to a smaller size by maxWidth, maxHeight, or maxArea
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setSizeMaxNotUpscaled() {
    baseBuilder.setSizeMax();
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
  public ImageRequestBuilder_v3 setSizeScaledExactUpscaled(Float w, Float h) throws IllegalArgumentException, OperationNotSupportedException {
    BaseImageRequestValidators.validateWidthAndHeightImageDimenFloats(w, h);
    if (validators != null) {
      validators.validateSizeScaledExact(w, h, true);
    }
    baseBuilder.setSizeScaledExact(w, h, PREFIX_UPSCALING_MODIFIER);
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
  public ImageRequestBuilder_v3 setSizeScaledExactNotUpscaled(Float w, Float h) throws IllegalArgumentException, OperationNotSupportedException {
    BaseImageRequestValidators.validateWidthAndHeightImageDimenFloats(w, h);
    if (validators != null) {
      validators.validateSizeScaledExact(w, h, false);
    }
    baseBuilder.setSizeScaledExact(w, h);
    return this;
  }

  /**
   * The extracted region is scaled so that the width and height of the returned image are not greater than w and h, while maintaining the aspect ratio. The returned image must be as large as possible but not larger than the extracted region, w or h, or server-imposed limits.
   *
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException Behaviour of server when both width and height are overridable is undefined
   */
  public ImageRequestBuilder_v3 setSizeScaledBestFitNotUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException, OperationNotSupportedException {
    BaseImageRequestValidators.validateBothWidthAndHeightNotOverridable(isWidthOverridable, isHeightOverridable);
    if (validators != null) {
      validators.validateSizeScaledBestFit(w, h, isWidthOverridable, isHeightOverridable, false);
    }
    // If both width and height cannot be overridden by the server then it is the same case as exact scaling.
    if (!isWidthOverridable && !isHeightOverridable) {
      return this.setSizeScaledExactNotUpscaled(w, h);
    }
    baseBuilder.setSizeScaledBestFit(w, h, isWidthOverridable, isHeightOverridable);
    return this;
  }

  /**
   * The extracted region is scaled so that the width and height of the returned image are not greater than w and h, while maintaining the aspect ratio. The returned image must be as large as possible but not larger than w, h, or server-imposed limits.
   *
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException Behaviour of server when both width and height are overridable is undefined
   */
  public ImageRequestBuilder_v3 setSizeScaledBestFitUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException, OperationNotSupportedException {
    BaseImageRequestValidators.validateBothWidthAndHeightNotOverridable(isWidthOverridable, isHeightOverridable);
    if (validators != null) {
      validators.validateSizeScaledBestFit(w, h, isWidthOverridable, isHeightOverridable, true);
    }
    // If both width and height cannot be overridden by the server then it is the same case as exact scaling.
    if (!isWidthOverridable && !isHeightOverridable) {
      return this.setSizeScaledExactUpscaled(w, h);
    }
    baseBuilder.setSizeScaledBestFit(w, h, isWidthOverridable, isHeightOverridable, PREFIX_UPSCALING_MODIFIER);
    return this;
  }

  /**
   * The width and height of the returned image is scaled to n percent of the width and height of the extracted region. The value of n must not be greater than 100.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setSizePercentageNotUpscaled(float n) throws OperationNotSupportedException {
    BaseImageRequestValidators.validatePercentageValueGreaterThanZero(n);
    if (validators != null) {
      validators.validateSizePercentage(n, false);
    }
    baseBuilder.setSizePercentage(n);
    return this;
  }

  /**
   * The width and height of the returned image is scaled to n percent of the width and height of the extracted region. For values of n greater than 100, the extracted region is upscaled.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  public ImageRequestBuilder_v3 setSizePercentageUpscaled(float n) throws OperationNotSupportedException {
    BaseImageRequestValidators.validatePercentageValueGreaterThanZero(n);
    if (validators != null) {
      validators.validateSizePercentage(n, true);
    }
    baseBuilder.setSizePercentage(n, PREFIX_UPSCALING_MODIFIER);
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
    BaseImageRequestValidators.validateRotationDegrees(degree);
    if (validators != null) {
      validators.validateServerSupportsRotation(degree, mirror);
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

  /**
   * This method builds a new ImageRequest with the parameters set using the dedicated setter methods
   */
  public ImageRequest build() {
    ImageRequest build = baseBuilder.build();
    if (this.size != null) {
      build.setSize(this.size);
    }
    return build;
  }

  /**
   * Get the {@link ImageApiVersion} of the ImageInformation
   */
  public ImageApiVersion getImageApiVersion() {
    return new ImageApiVersion(IMAGE_API_VERSION.THREE_POINT_ZERO);
  }

  private static class Validators extends BaseImageRequestValidators {

    private static final String ERROR_UPSCALING_NOT_SUPPORTED = "Server does not support requesting for image sizes larger than the image's max size";

    private final ImageInformation imageInformation;

    public Validators(ImageInformation imageInformation) throws IllegalArgumentException {
      super(imageInformation);
      this.imageInformation = imageInformation;
    }

    @Override
    public void validateServerSupportsQuality(String quality) throws OperationNotSupportedException {
      // Server can return any quality (the quality with the most color data available) for a color request but doesn't have to explicitly list the color quality in the profiles data
      if (quality.equals(QUALITY_COLOR)) {
        return;
      }
      super.validateServerSupportsQuality(quality);
    }

    public boolean validateSizeMax(boolean upscaling) throws OperationNotSupportedException {
      if (upscaling) {
        validateServerSupportsFeature(SUPPORTS_SIZE_UPSCALING, ERROR_UPSCALING_NOT_SUPPORTED);
      }
      return true;
    }

    public boolean validateSizeScaledExact(Float width, Float height, boolean upscaling) throws OperationNotSupportedException {
      boolean isWidthValid = isImageDimenValidFloat(width);
      boolean isHeightValid = isImageDimenValidFloat(height);
      if (!isWidthValid) {
        validateServerSupportsFeature(SUPPORTS_SIZE_BY_H, "Server does not support requesting for image sizes by height alone");
      }
      if (!isHeightValid) {
        validateServerSupportsFeature(SUPPORTS_SIZE_BY_W, "Server does not support requesting for image sizes by width alone");
      }
      if (upscaling && width > imageInformation.getWidth() || height > imageInformation.getHeight()) {
        validateServerSupportsFeature(SUPPORTS_SIZE_UPSCALING, ERROR_UPSCALING_NOT_SUPPORTED);
      }
      validateServerSupportsFeature(SUPPORTS_SIZE_BY_WH, "Server does not support requesting for image sizes using width and height parameters");
      return true;
    }

    public boolean validateSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable, boolean upscaling) throws OperationNotSupportedException {
      if (upscaling && (width > imageInformation.getWidth() || height > imageInformation.getHeight())) {
        validateServerSupportsFeature(SUPPORTS_SIZE_UPSCALING, ERROR_UPSCALING_NOT_SUPPORTED);
      }
      if (isWidthOverridable || isHeightOverridable) {
        validateServerSupportsFeature(SUPPORTS_SIZE_BY_CONFINED_WH, "Server does not support requesting images with overridable width or height parameters");
      }
      return true;
    }

    public boolean validateSizePercentage(float n, boolean upscaling) throws OperationNotSupportedException {
      validateServerSupportsFeature(SUPPORTS_SIZE_BY_PCT, "Server does not support requesting for image size using percentages");
      if (upscaling && n > 100) {
        validateServerSupportsFeature(SUPPORTS_SIZE_UPSCALING, ERROR_UPSCALING_NOT_SUPPORTED);
      }
      return true;
    }

  }
}
