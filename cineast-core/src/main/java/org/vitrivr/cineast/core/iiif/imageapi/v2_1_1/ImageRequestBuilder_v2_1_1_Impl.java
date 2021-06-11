package org.vitrivr.cineast.core.iiif.imageapi.v2_1_1;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl.isImageDimenValidFloat;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_MIRRORING;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_REGION_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_REGION_BY_PX;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_REGION_SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_ROTATION_ARBITRARY;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_ROTATION_BY_90s;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_ABOVE_FULL;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_CONFINED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_DISTORTED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_H;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_W;
import static org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem.SUPPORTS_SIZE_BY_WH;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder;
import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilderImpl;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem;
import org.vitrivr.cineast.core.iiif.imageapi.ImageRequest;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 09.06.21
 */
public class ImageRequestBuilder_v2_1_1_Impl implements ImageRequestBuilder_v2_1_1 {

  private static final Logger LOGGER = LogManager.getLogger();
  private final BaseImageRequestBuilder baseBuilder;
  private ImageInformation imageInformation = null;
  private String size;

  public ImageRequestBuilder_v2_1_1_Impl(String baseUrl) {
    this.baseBuilder = new BaseImageRequestBuilderImpl(baseUrl);
  }

  public ImageRequestBuilder_v2_1_1_Impl(ImageInformation imageInformation) {
    this(imageInformation.getAtId());
    this.imageInformation = imageInformation;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionFull() {
    baseBuilder.setRegionFull();
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionSquare() throws IllegalArgumentException {
    if (imageInformation != null) {
      try {
        boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_REGION_SQUARE);
        if (!isSupported) {
          throw new IllegalArgumentException("Server does not support explicitly requesting square regions of images");
        }
      } catch (UnsupportedOperationException e) {
        LOGGER.debug(e.getMessage());
      }
    }
    baseBuilder.setRegionSquare();
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionAbsolute(float x, float y, float w, float h) throws IllegalArgumentException {
    if (imageInformation != null) {
      try {
        boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_REGION_BY_PX);
        if (!isSupported) {
          throw new IllegalArgumentException("Server does not support requesting regions of images using pixel dimensions");
        }
      } catch (UnsupportedOperationException e) {
        LOGGER.debug(e.getMessage());
      }
    }
    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("Width and height must be greater than 0");
    }
    if (imageInformation != null && (w > imageInformation.getWidth() && h > imageInformation.getHeight())) {
      throw new IllegalArgumentException("Request region is entirely outside the image's reported dimensional bounds");
    }
    baseBuilder.setRegionAbsolute(x, y, w, h);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRegionPercentage(float x, float y, float w, float h) throws IllegalArgumentException {
    if (imageInformation != null) {
      try {
        boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_REGION_BY_PCT);
        if (!isSupported) {
          throw new IllegalArgumentException("Server does not support requests for regions of images by percentage.");
        }
      } catch (UnsupportedOperationException e) {
        LOGGER.debug(e.getMessage());
      }
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
    if (imageInformation != null) {
      if (!isWidthValid) {
        try {
          boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_SIZE_BY_H);
          if (!isSupported) {
            throw new IllegalArgumentException("Server does not support requesting for image sizes by height alone");
          }
        } catch (UnsupportedOperationException e) {
          LOGGER.debug(e.getMessage());
        }
      }
      if (!isHeightValid) {
        try {
          boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_SIZE_BY_W);
          if (!isSupported) {
            throw new IllegalArgumentException("Server does not support requesting for image sizes by width alone");
          }
        } catch (UnsupportedOperationException e) {
          LOGGER.debug(e.getMessage());
        }
      }
      if (width > imageInformation.getWidth() || height > imageInformation.getHeight()) {
        try {
          boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_SIZE_ABOVE_FULL);
          if (!isSupported) {
            throw new IllegalArgumentException("Server does not support requesting for image sizes about the image's full size");
          }
        } catch (UnsupportedOperationException e) {
          LOGGER.debug(e.getMessage());
        }
      }
      float requestAspectRatio = width / height;
      float originalAspectRatio = (float) imageInformation.getWidth() / imageInformation.getHeight();
      if (requestAspectRatio != originalAspectRatio) {
        try {
          boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_SIZE_BY_DISTORTED_WH);
          if (!isSupported) {
            throw new IllegalArgumentException("Server does not support requesting for image sizes that would distort the image");
          }
        } catch (UnsupportedOperationException e) {
          LOGGER.debug(e.getMessage());
        }
      } else {
        try {
          boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_SIZE_BY_WH);
          if (!isSupported) {
            throw new IllegalArgumentException("Server does not support requesting for image sizes using width and height parameters");
          }
        } catch (UnsupportedOperationException e) {
          LOGGER.debug(e.getMessage());
        }
      }
    }
    baseBuilder.setSizeScaledExact(width, height);
    return this;
  }

  public ImageRequestBuilder_v2_1_1_Impl setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException {
    if (imageInformation != null) {
      if (width > imageInformation.getWidth() || height > imageInformation.getHeight()) {
        try {
          boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_SIZE_ABOVE_FULL);
          if (!isSupported) {
            throw new IllegalArgumentException("Server does not support requesting for image sizes about the image's full size");
          }
        } catch (UnsupportedOperationException e) {
          LOGGER.debug(e.getMessage());
        }
      }
      if (isWidthOverridable || isHeightOverridable) {
        try {
          boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_SIZE_BY_CONFINED_WH);
          if (!isSupported) {
            throw new IllegalArgumentException("Server does not support requesting images with overridable width or height parameters");
          }
        } catch (UnsupportedOperationException e) {
          LOGGER.debug(e.getMessage());
        }
      }
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
    if (imageInformation != null) {
      try {
        boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_SIZE_BY_PCT);
        if (!isSupported) {
          throw new IllegalArgumentException("Server does not support requesting for image size using percentages");
        }
      } catch (UnsupportedOperationException e) {
        LOGGER.debug(e.getMessage());
      }
      if (n > 100) {
        try {
          boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_SIZE_ABOVE_FULL);
          if (!isSupported) {
            throw new IllegalArgumentException("Server does not support requesting for image sizes about the image's full size");
          }
        } catch (UnsupportedOperationException e) {
          LOGGER.debug(e.getMessage());
        }
      }
    }
    if (n <= 0) {
      throw new IllegalArgumentException("Percentage value has to be greater than 0");
    }
    baseBuilder.setSizePercentage(n);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setRotation(float degree, boolean mirror) throws IllegalArgumentException {
    if (imageInformation != null) {
      if (mirror) {
        try {
          boolean isSupported = imageInformation.isFeatureSupported(SUPPORTS_MIRRORING);
          if (!isSupported) {
            throw new IllegalArgumentException("Mirroring of images is not supported by the server");
          }
        } catch (UnsupportedOperationException e) {
          LOGGER.debug(e.getMessage());
        }
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
    }
    if (degree < 0 || degree > 360) {
      throw new IllegalArgumentException("Rotation value can only be between 0° and 360°!");
    }
    baseBuilder.setRotation(degree, mirror);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setQuality(String quality) throws IllegalArgumentException {
    if (imageInformation != null) {
      List<ProfileItem> profiles = imageInformation.getProfile().second;
      boolean isQualitySupported = profiles.stream().anyMatch(item -> item.getQualities().stream().anyMatch(q -> q.equals(quality)));
      if (!isQualitySupported) {
        throw new IllegalArgumentException("Requested quality is not supported by the server");
      }
    }
    baseBuilder.setQuality(quality);
    return this;
  }

  @Override
  public ImageRequestBuilder_v2_1_1_Impl setFormat(String format) throws IllegalArgumentException {
    if (imageInformation != null) {
      List<ProfileItem> profiles = imageInformation.getProfile().second;
      boolean isExtensionSupported = profiles.stream().anyMatch(item -> item.getFormats().stream().anyMatch(q -> q.equals(format)));
      if (!isExtensionSupported) {
        throw new IllegalArgumentException("Requested format is not supported by the server");
      }
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
}
