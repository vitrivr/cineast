package org.vitrivr.cineast.core.iiif.imageapi;

import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_MIRRORING;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_REGION_BY_PX;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_ROTATION_ARBITRARY;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_ROTATION_BY_90s;

import java.util.List;
import javax.naming.OperationNotSupportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem;

/**
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

  public void validateServerSupportsRegionAbsolute(float w, float h) throws OperationNotSupportedException {
    validateServerSupportsFeature(SUPPORTS_REGION_BY_PX, "Server does not support requesting regions of images using pixel dimensions");
    if (w > imageInformation.getWidth() && h > imageInformation.getHeight()) {
      throw new OperationNotSupportedException("Request region is entirely outside the image's reported dimensional bounds");
    }
  }

  public void validateServerSupportsQuality(String quality) throws OperationNotSupportedException {
    List<ProfileItem> profiles = imageInformation.getProfile().second;
    boolean isQualitySupported = profiles.stream().anyMatch(item -> item.getQualities().stream().anyMatch(q -> q.equals(quality)));
    if (!isQualitySupported) {
      throw new OperationNotSupportedException("Requested quality is not supported by the server");
    }
  }

  public void validateServerSupportsFormat(String format) throws OperationNotSupportedException {
    List<ProfileItem> profiles = imageInformation.getProfile().second;
    boolean isExtensionSupported = profiles.stream().anyMatch(item -> item.getFormats().stream().anyMatch(q -> q.equals(format)));
    if (!isExtensionSupported) {
      throw new OperationNotSupportedException("Requested format is not supported by the server");
    }
  }

  public void validateSetRotation(float degree, boolean mirror) throws OperationNotSupportedException {
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