package org.vitrivr.cineast.core.iiif.imageapi.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 13.06.21
 */
public class ImageInformation_v3 implements ImageInformation {

  /** The maximum area in pixels supported for this image. Clients must not expect requests with a width*height greater than this value to be supported. */
  @Getter
  @Setter
  @JsonProperty
  public long maxArea;
  /**
   * The @context tells Linked Data processors how to interpret the image information. If extensions are used then their context definitions should be included in this top-level @context property.
   */
  @Getter
  @Setter
  @JsonProperty(value = "@context", required = true)
  private String atContext;
  /**
   * The base URI of the image as defined in URI Syntax, including scheme, server, prefix and identifier without a trailing slash.
   */
  @Getter
  @Setter
  @JsonProperty(value = "id", required = true)
  private String id;
  /**
   * The type for the Image. If present, the value must be the string iiif:Image.
   */
  @Getter
  @Setter
  @JsonProperty(value = "type", required = true)
  private String type;
  /**
   * The URI http://iiif.io/api/image which can be used to determine that the document describes an image service which is a version of the IIIF Image API.
   */
  @Getter
  @Setter
  @JsonProperty(required = true)
  private String protocol;
  /** A string indicating the highest compliance level which is fully supported by the service. The value must be one of level0, level1, or level2. */
  @Getter
  @Setter
  @JsonProperty(required = true)
  private String profile;
  /**
   * The width in pixels of the full image content, given as an integer.
   */
  @Getter
  @Setter
  @JsonProperty(required = true)
  private long width;
  /**
   * The height in pixels of the full image content, given as an integer.
   */
  @Getter
  @Setter
  @JsonProperty(required = true)
  private long height;
  /** The maximum width in pixels supported for this image. Clients must not expect requests with a width greater than this value to be supported. maxWidth must be specified if maxHeight is specified. */
  @Getter
  @Setter
  @JsonProperty
  private long maxWidth;
  /** The maximum height in pixels supported for this image. Clients must not expect requests with a height greater than this value to be supported. */
  @Setter
  @JsonProperty
  private long maxHeight;

  /**
   * A set of height and width pairs the client should use in the size parameter to request complete images at different sizes that the server has available. This may be used to let a client know the sizes that are available when the server does not support requests for arbitrary sizes, or simply as a hint that requesting an image of this size may result in a faster response. A request constructed with the w,h syntax using these sizes must be supported by the server, even if arbitrary width and height are not.
   */
  @Getter
  @Setter
  @JsonProperty
  private List<SizesItem> sizes;

  /**
   * A set of descriptions of the parameters to use to request regions of the image (tiles) that are efficient for the server to deliver. Each description gives a width, optionally a height for non-square tiles, and a set of scale factors at which tiles of those dimensions are available.
   */
  @Getter
  @Setter
  @JsonProperty
  private List<TilesItem> tiles;

  public long getMaxHeight() {
    // If maxWidth is specified and maxHeight is not, then clients should infer that maxHeight = maxWidth
    if (maxHeight == 0 && maxWidth != 0) {
      maxHeight = maxWidth;
    }
    return maxHeight;
  }

  @Override
  public boolean isFeatureSupported(String feature) {
    return false;
  }

  @Override
  public boolean isQualitySupported(String quality) {
    return false;
  }

  @Override
  public boolean isFormatSupported(String format) {
    return false;
  }

  @Override
  public ImageApiVersion getImageApiVersion() {
    return new ImageApiVersion(IMAGE_API_VERSION.THREE_POINT_ZERO);
  }
}
