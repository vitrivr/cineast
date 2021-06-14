package org.vitrivr.cineast.core.iiif.imageapi.v2;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl.isImageDimenValidFloat;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_REGION_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_REGION_SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_ABOVE_FULL;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_CONFINED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_DISTORTED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_H;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_W;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_WH;

import javax.naming.OperationNotSupportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestValidators;
import org.vitrivr.cineast.core.iiif.imageapi.ImageRequest;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 09.06.21
 */
public class ImageRequestBuilder_v2 {

  public static final String SIZE_MAX = "max";
  public static final String SIZE_PERCENTAGE = "pct:";
  public static final String SIZE_FULL = "full";
  private static final Logger LOGGER = LogManager.getLogger();
  private final BaseImageRequestBuilder baseBuilder;
  private String size;
  private Validators validators;

  public ImageRequestBuilder_v2(String baseUrl) {
    this.baseBuilder = new BaseImageRequestBuilderImpl(baseUrl);
  }

  public ImageRequestBuilder_v2(ImageInformation_v2 imageInformation) throws IllegalArgumentException {
    this(imageInformation.getAtId());
    this.validators = new Validators(imageInformation);
  }

  /**
   * The complete image is returned, without any cropping.
   *
   * @return this {@link ImageRequestBuilder_v2}
   */
  public ImageRequestBuilder_v2 setRegionFull() {
    baseBuilder.setRegionFull();
    return this;
  }

  /**
   * The region is defined as an area where the width and height are both equal to the length of the shorter dimension of the complete image. The region may be positioned anywhere in the longer dimension of the image content at the server’s discretion, and centered is often a reasonable default.
   *
   * @return this {@link ImageRequestBuilder_v2}
   */
  public ImageRequestBuilder_v2 setRegionSquare() throws IllegalArgumentException, OperationNotSupportedException {
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
   * @return this {@link ImageRequestBuilder_v2}
   */
  public ImageRequestBuilder_v2 setRegionAbsolute(float x, float y, float w, float h) throws IllegalArgumentException, OperationNotSupportedException {
    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("Width and height must be greater than 0");
    }
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
   * @return this {@link ImageRequestBuilder_v2}
   */
  public ImageRequestBuilder_v2 setRegionPercentage(float x, float y, float w, float h) throws IllegalArgumentException, OperationNotSupportedException {
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

  /** The image or region is not scaled, and is returned at its full size. */
  public ImageRequestBuilder_v2 setSizeFull() {
    this.size = SIZE_FULL;
    return this;
  }

  /**
   * The image or region is returned at the maximum size available, as indicated by maxWidth, maxHeight, maxArea in the profile description. This is the same as full if none of these properties are provided.
   *
   * @return this {@link ImageRequestBuilder_v2}
   */
  public ImageRequestBuilder_v2 setSizeMax() {
    baseBuilder.setSizeMax();
    return this;
  }

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @return this {@link ImageRequestBuilder_v2}
   * @throws IllegalArgumentException If both height and width are undefined then an IllegalArgumentException is thrown
   */
  public ImageRequestBuilder_v2 setSizeScaledExact(Float width, Float height) throws IllegalArgumentException, OperationNotSupportedException {
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

  /**
   * The image content is scaled for the best fit such that the resulting width and height are less than or equal to the requested width and height. The exact scaling may be determined by the service provider, based on characteristics including image quality and system performance. The dimensions of the returned image content are calculated to maintain the aspect ratio of the extracted region.
   *
   * @return this {@link ImageRequestBuilder_v2}
   * @throws IllegalArgumentException Behaviour of server when both width and height are overridable is undefined
   */
  public ImageRequestBuilder_v2 setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException, OperationNotSupportedException {
    // Behaviour of server when both width and height are overridable is undefined. Thus, user should be forced to some other method such as setSizeMax.
    if (isWidthOverridable && isHeightOverridable) {
      throw new IllegalArgumentException("Both width and height cannot be overridable!");
    }
    if (validators != null) {
      validators.validateSizeScaledBestFit(width, height, isWidthOverridable, isHeightOverridable);
    }
    // If both width and height cannot be overridden by the server then it is the same case as exact scaling.
    if (!isWidthOverridable && !isHeightOverridable) {
      return this.setSizeScaledExact(width, height);
    }
    baseBuilder.setSizeScaledBestFit(width, height, isWidthOverridable, isHeightOverridable);
    return this;
  }

  /**
   * The width and height of the returned image is scaled to n% of the width and height of the extracted region. The aspect ratio of the returned image is the same as that of the extracted region.
   *
   * @return this {@link ImageRequestBuilder_v2}
   */
  public ImageRequestBuilder_v2 setSizePercentage(float n) throws IllegalArgumentException, OperationNotSupportedException {
    if (n <= 0) {
      throw new IllegalArgumentException("Percentage value has to be greater than 0");
    }
    if (validators != null) {
      validators.validateSizePercentage(n);
    }
    baseBuilder.setSizePercentage(n);
    return this;
  }

  /**
   * This method is used to specify the mirroring and rotation applied to the image.
   *
   * @param degree Represents the number of degrees of clockwise rotation, and may be any floating point number from 0 to 360.
   * @param mirror Indicates if that the image should be mirrored by reflection on the vertical axis before any rotation is applied.
   * @return this {@link ImageRequestBuilder_v2}
   */
  public ImageRequestBuilder_v2 setRotation(float degree, boolean mirror) throws IllegalArgumentException, OperationNotSupportedException {
    if (degree < 0 || degree > 360) {
      throw new IllegalArgumentException("Rotation value can only be between 0° and 360°!");
    }
    if (validators != null) {
      validators.validateSetRotation(degree, mirror);
    }
    baseBuilder.setRotation(degree, mirror);
    return this;
  }

  /**
   * This method is used to specify the quality of the image.
   *
   * @param quality The quality of the image
   * @return this {@link ImageRequestBuilder_v2}
   */
  public ImageRequestBuilder_v2 setQuality(String quality) throws IllegalArgumentException, OperationNotSupportedException {
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
   * @return this {@link ImageRequestBuilder_v2}
   */
  public ImageRequestBuilder_v2 setFormat(String format) throws IllegalArgumentException, OperationNotSupportedException {
    if (validators != null) {
      validators.validateServerSupportsFormat(format);
    }
    baseBuilder.setFormat(format);
    return this;
  }

  /** This method builds a new ImageRequest with the parameters set using the dedicated setter methods */
  public ImageRequest build() {
    ImageRequest imageRequest = baseBuilder.build();
    if (this.size != null && !this.size.isEmpty()) {
      imageRequest.setSize(this.size);
    }
    return imageRequest;
  }

  private static class Validators extends BaseImageRequestValidators {

    private final ImageInformation_v2 imageInformation;

    public Validators(ImageInformation_v2 imageInformation) throws IllegalArgumentException {
      super(imageInformation);
      this.imageInformation = imageInformation;
    }

    private void validateSizeScaledExact(Float width, Float height, boolean isWidthValid, boolean isHeightValid) throws OperationNotSupportedException {
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

    public boolean validateSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) throws OperationNotSupportedException {
      if (width > imageInformation.getWidth() || height > imageInformation.getHeight()) {
        validateServerSupportsFeature(SUPPORTS_SIZE_ABOVE_FULL, "Server does not support requesting for image sizes about the image's full size");
      }
      if (isWidthOverridable || isHeightOverridable) {
        validateServerSupportsFeature(SUPPORTS_SIZE_BY_CONFINED_WH, "Server does not support requesting images with overridable width or height parameters");
      }
      return false;
    }

    public void validateSizePercentage(float n) throws OperationNotSupportedException {
      validateServerSupportsFeature(SUPPORTS_SIZE_BY_PCT, "Server does not support requesting for image size using percentages");
      if (n > 100) {
        validateServerSupportsFeature(SUPPORTS_SIZE_ABOVE_FULL, "Server does not support requesting for image sizes about the image's full size");
      }
    }
  }
}
