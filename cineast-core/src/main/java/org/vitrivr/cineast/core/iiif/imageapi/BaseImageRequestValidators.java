package org.vitrivr.cineast.core.iiif.imageapi;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.isImageDimenValidFloat;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_MIRRORING;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_REGION_BY_PX;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_ROTATION_ARBITRARY;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_ROTATION_BY_90s;

import javax.naming.OperationNotSupportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common Image request parameter validation methods used by both API 2.x and 3.x
 *
 * @author singaltanmay
 * @version 1.0
 * @created 11.06.21
 */
public class BaseImageRequestValidators {

  private static final Logger LOGGER = LogManager.getLogger();
  private final ImageInformation imageInformation;

  public BaseImageRequestValidators(ImageInformation imageInformation) throws IllegalArgumentException {
    if (imageInformation == null) {
      throw new IllegalArgumentException("ImageInformation cannot be null");
    }
    this.imageInformation = imageInformation;
  }

  /**
   * Validates that the width and height of the image are greater than zero
   */
  public static boolean validateWidthAndHeightGreaterThanZero(float w, float h) throws IllegalArgumentException {
    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("Width and height must be greater than 0");
    }
    return true;
  }

  /**
   * Validates that at least some portion of the requested image lies in the bounds of the original image
   */
  public static boolean validatePercentageBoundsValid(float x, float y, float w, float h) {
    if (x < 0 || x > 100 || y < 0 || y > 100) {
      throw new IllegalArgumentException("Value should lie between 0 and 100");
    }
    if (x == 100 || y == 100) {
      throw new IllegalArgumentException("Request region is entirely outside the image's reported dimensional bounds");
    }
    if (w <= 0 || w > 100 || h <= 0 || h > 100) {
      throw new IllegalArgumentException("Height and width of the image must belong in the range (0, 100]");
    }
    return true;
  }

  /**
   * Validates that width and height Floats pass isImageDimenValidFloat()
   */
  public static boolean validateWidthAndHeightImageDimenFloats(Float width, Float height) {
    boolean isWidthValid = isImageDimenValidFloat(width);
    boolean isHeightValid = isImageDimenValidFloat(height);
    // Behaviour of server when neither width or height are provided is undefined. Thus, user should be forced to some other method such as setSizeMax.
    if (!isWidthValid && !isHeightValid) {
      throw new IllegalArgumentException("Either width or height must be a valid float value!");
    }
    return true;
  }

  /**
   * Behaviour of server when both width and height are overridable is undefined. Thus, user should be forced to some other method such as setSizeMax.
   */
  public static boolean validateBothWidthAndHeightNotOverridable(boolean isWidthOverridable, boolean isHeightOverridable) {
    if (isWidthOverridable && isHeightOverridable) {
      throw new IllegalArgumentException("Both width and height cannot be overridable!");
    }
    return true;
  }

  /** Validates that the float value of a percentage is greater than zero */
  public static boolean validatePercentageValueGreaterThanZero(float n) {
    if (n <= 0) {
      throw new IllegalArgumentException("Percentage value has to be greater than 0");
    }
    return true;
  }

  /** Validates that the degree of rotation is >= 0 and < 360 */
  public static boolean validateRotationDegrees(float degree) {
    if (degree < 0 || degree > 360) {
      throw new IllegalArgumentException("Rotation value can only be between 0° and 360°!");
    }
    return true;
  }

  public boolean validateDimensWithinMaxValues(float w, float h) throws OperationNotSupportedException {
    Long maxWidth = imageInformation.getMaxWidth();
    Long maxHeight = imageInformation.getMaxHeight();
    Long maxArea = imageInformation.getMaxArea();
    boolean isMaxWidthValid = maxWidth != null && isImageDimenValidFloat(maxWidth.floatValue());
    boolean isMaxHeightValid = maxHeight != null && isImageDimenValidFloat(maxHeight.floatValue());
    boolean isMaxAreaValid = maxArea != null && isImageDimenValidFloat(maxArea.floatValue());
    if (isMaxWidthValid && w > maxWidth) {
      throw new OperationNotSupportedException("Requested image width exceeds the maxWidth supported by the server");
    }
    if (isMaxHeightValid && h > maxHeight) {
      throw new OperationNotSupportedException("Requested image height exceeds the maxHeight supported by the server");
    }
    if (isMaxAreaValid && (w * h) > maxArea) {
      throw new OperationNotSupportedException("Requested image area exceeds the maxArea supported by the server");
    }
    return true;
  }

  public boolean validateServerSupportsFeature(String featureName, String errorMessage) throws OperationNotSupportedException {
    boolean isSupported;
    try {
      isSupported = imageInformation.isFeatureSupported(featureName);
      if (!isSupported) {
        throw new OperationNotSupportedException(errorMessage);
      }
    } catch (NullPointerException e) {
      LOGGER.debug(e.getMessage());
      isSupported = true;
    }
    return isSupported;
  }

  public boolean validateServerSupportsRegionAbsolute(float w, float h) throws OperationNotSupportedException {
    validateServerSupportsFeature(SUPPORTS_REGION_BY_PX, "Server does not support requesting regions of images using pixel dimensions");
    if (w > imageInformation.getWidth() && h > imageInformation.getHeight()) {
      throw new OperationNotSupportedException("Request region is entirely outside the image's reported dimensional bounds");
    }
    return validateDimensWithinMaxValues(w, h);
  }

  public void validateServerSupportsQuality(String quality) throws OperationNotSupportedException {
    if (!imageInformation.isQualitySupported(quality)) {
      throw new OperationNotSupportedException("Requested quality is not supported by the server");
    }
  }

  public void validateServerSupportsFormat(String format) throws OperationNotSupportedException {
    if (!imageInformation.isFormatSupported(format)) {
      throw new OperationNotSupportedException("Requested format is not supported by the server");
    }
  }

  public void validateServerSupportsRotation(float degree, boolean mirror) throws OperationNotSupportedException {
    if (mirror) {
      validateServerSupportsFeature(SUPPORTS_MIRRORING, "Mirroring of images is not supported by the server");
    }
    try {
      float offsetBy90 = Math.abs(degree % 90);
      boolean is90sOffsetSupported = false;
      if (offsetBy90 == 0) {
        is90sOffsetSupported = imageInformation.isFeatureSupported(SUPPORTS_ROTATION_BY_90s);
      }
      if (offsetBy90 != 0 || !is90sOffsetSupported) {
        boolean isArbitrarySupported = imageInformation.isFeatureSupported(SUPPORTS_ROTATION_ARBITRARY);
        if (!isArbitrarySupported) {
          String message;
          if (is90sOffsetSupported) {
            message = "Server only supports rotating images in multiples of 90°s";
          } else {
            message = "Server does not support rotating the image by specified amount";
          }
          throw new OperationNotSupportedException(message);
        }
      }
    } catch (NullPointerException e) {
      LOGGER.debug(e.getMessage());
    }
  }

}