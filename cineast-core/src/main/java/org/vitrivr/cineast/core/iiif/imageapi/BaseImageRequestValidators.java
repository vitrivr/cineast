package org.vitrivr.cineast.core.iiif.imageapi;

import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_MIRRORING;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_REGION_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_REGION_BY_PX;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_ROTATION_ARBITRARY;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_ROTATION_BY_90s;

import java.util.List;
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

  public BaseImageRequestValidators(ImageInformation imageInformation) {
    if (imageInformation == null) {
      throw new IllegalArgumentException("ImageInformation cannot be null");
    }
    this.imageInformation = imageInformation;
  }

  public boolean validateServerSupportsFeature(String featureName, String errorMessage) {
    boolean isSupported = false;
    try {
      isSupported = imageInformation.isFeatureSupported(featureName);
      if (!isSupported) {
        throw new IllegalArgumentException(errorMessage);
      }
    } catch (UnsupportedOperationException e) {
      LOGGER.debug(e.getMessage());
    }
    return isSupported;
  }

  public void validateServerSupportsRegionAbsolute(float w, float h) {
    if (imageInformation != null) {
      validateServerSupportsFeature(SUPPORTS_REGION_BY_PX, "Server does not support requesting regions of images using pixel dimensions");
    }
    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("Width and height must be greater than 0");
    }
    if (imageInformation != null && (w > imageInformation.getWidth() && h > imageInformation.getHeight())) {
      throw new IllegalArgumentException("Request region is entirely outside the image's reported dimensional bounds");
    }
  }

  public void validateServerSupportsRegionPercentage(float x, float y, float w, float h) {
    if (imageInformation != null) {
      validateServerSupportsFeature(SUPPORTS_REGION_BY_PCT, "Server does not support requests for regions of images by percentage.");
    }
    if (x < 0 || x > 100 || y < 0 || y > 100) {
      throw new IllegalArgumentException("Value should lie between 0 and 100");
    }
    if (x == 100 || y == 100) {
      throw new IllegalArgumentException("Request region is entirely outside the image's reported dimensional bounds");
    }
    if (w <= 0 || w > 100 || h <= 0 || h > 100) {
      throw new IllegalArgumentException("Height and width of the image must belong in the range (0, 100]");
    }
  }

  public void validateServerSupportsQuality(String quality) {
    List<ProfileItem> profiles = imageInformation.getProfile().second;
    boolean isQualitySupported = profiles.stream().anyMatch(item -> item.getQualities().stream().anyMatch(q -> q.equals(quality)));
    if (!isQualitySupported) {
      throw new IllegalArgumentException("Requested quality is not supported by the server");
    }
  }

  public void validateServerSupportsFormat(String format) {
    List<ProfileItem> profiles = imageInformation.getProfile().second;
    boolean isExtensionSupported = profiles.stream().anyMatch(item -> item.getFormats().stream().anyMatch(q -> q.equals(format)));
    if (!isExtensionSupported) {
      throw new IllegalArgumentException("Requested format is not supported by the server");
    }
  }

  public void validateSetRotation(float degree, boolean mirror) {
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
          throw new IllegalArgumentException(message);
        }
      }
    } catch (UnsupportedOperationException e) {
      LOGGER.debug(e.getMessage());
    }
    if (degree < 0 || degree > 360) {
      throw new IllegalArgumentException("Rotation value can only be between 0° and 360°!");
    }
  }

}