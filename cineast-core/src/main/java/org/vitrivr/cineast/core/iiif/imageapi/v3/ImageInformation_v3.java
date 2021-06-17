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

  // The base URI of the service will redirect to the image information document.
  public static final String SUPPORTS_BASE_URI_REDIRECT = "baseUriRedirect";
  // The canonical image URI HTTP link header is provided on image responses.
  public static final String SUPPORTS_CANONICAL_LINK_HEADER = "canonicalLinkHeader";
  // The CORS HTTP header is provided on all responses.
  public static final String SUPPORTS_CORS = "cors";
  // The JSON-LD media type is provided when JSON-LD is requested.
  public static final String SUPPORTS_JSONLD_MEDIA_TYPE = "jsonldMediaType";
  // The image may be rotated around the vertical axis, resulting in a left-to-right mirroring of the content.
  public static final String SUPPORTS_MIRRORING = "mirroring";
  // The profile HTTP link header is provided on image responses.
  public static final String SUPPORTS_PROFILE_LINK_HEADER = "profileLinkHeader";
  // Regions of images may be requested by percentage.
  public static final String SUPPORTS_REGION_BY_PCT = "regionByPct";
  // Regions of images may be requested by pixel dimensions.
  public static final String SUPPORTS_REGION_BY_PX = "regionByPx";
  // A square region where the width and height are equal to the shorter dimension of the complete image content.
  public static final String SUPPORTS_REGION_SQUARE = "regionSquare";
  // Rotation of images may be requested by degrees other than multiples of 90.
  public static final String SUPPORTS_ROTATION_ARBITRARY = "rotationArbitrary";
  // Rotation of images may be requested by degrees in multiples of 90.
  public static final String SUPPORTS_ROTATION_BY_90s = "rotationBy90s";
  // Size of images may be requested in the form “!w,h”.
  public static final String SUPPORTS_SIZE_BY_CONFINED_WH = "sizeByConfinedWh";
  // Size of images may be requested in the form “,h”.
  public static final String SUPPORTS_SIZE_BY_H = "sizeByH";
  // Size of images may be requested in the form “pct:n”.
  public static final String SUPPORTS_SIZE_BY_PCT = "sizeByPct";
  // Size of images may be requested in the form “w,”.
  public static final String SUPPORTS_SIZE_BY_W = "sizeByW";
  // Size of images may be requested in the form “w,h” where the supplied w and h preserve the aspect ratio.
  public static final String SUPPORTS_SIZE_BY_WH = "sizeByWh";
  // Image sizes prefixed with ^ may be requested.
  public static final String SUPPORTS_SIZE_UPSCALING = "sizeUpscaling";

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
  private Long maxWidth;
  /** The maximum height in pixels supported for this image. Clients must not expect requests with a height greater than this value to be supported. */
  @Setter
  @JsonProperty
  private Long maxHeight;
  /** The maximum area in pixels supported for this image. Clients must not expect requests with a width*height greater than this value to be supported. */
  @Getter
  @Setter
  @JsonProperty
  private Long maxArea;

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

  /** The JSON response may have the preferredFormats property, which lists one or more format parameter values for this image service. This allows the publisher to express a preference for the format a client requests, for example to encourage use of a more efficient format such as webp, or to suggest a format that will give better results for the image content, such as lossless webp or png for line art or graphics. */
  @Getter
  @Setter
  @JsonProperty
  private List<String> preferredFormats;

  /** A string that identifies a license or rights statement that applies to the content of this image. */
  @Getter
  @Setter
  @JsonProperty
  private String rights;

  /** An array of strings that can be used as the quality parameter, in addition to default. */
  @Getter
  @Setter
  @JsonProperty
  private List<String> extraQualities;

  /** An array of strings that can be used as the format parameter, in addition to the ones specified in the referenced profile. */
  @Getter
  @Setter
  @JsonProperty
  private List<String> extraFormats;

  /** An array of strings identifying features supported by the service */
  @Getter
  @Setter
  @JsonProperty
  private List<String> extraFeatures;

  public Long getMaxHeight() {
    // If maxWidth is specified and maxHeight is not, then clients should infer that maxHeight = maxWidth
    if (maxHeight == 0 && maxWidth != 0) {
      maxHeight = maxWidth;
    }
    return maxHeight;
  }

  @Override
  public boolean isFeatureSupported(String feature) {
    if(ImageApiCompliance_v3.isFeatureSupported(feature, profile)) return true;
    boolean isSupported = true;
    if (extraFeatures != null && extraFeatures.size() != 0) {
      isSupported = extraFeatures.stream().anyMatch(it -> it.equals(feature));
    }
    return isSupported;
  }

  @Override
  public boolean isQualitySupported(String quality) {
    if(ImageApiCompliance_v3.isQualitySupported(quality, profile)) return true;
    boolean isSupported = true;
    if (extraQualities != null && extraQualities.size() != 0) {
      isSupported = extraQualities.stream().anyMatch(it -> it.equals(quality));
    }
    return isSupported;
  }

  @Override
  public boolean isFormatSupported(String format) {
    if(ImageApiCompliance_v3.isFormatSupported(format, profile)) return true;
    boolean isSupported = true;
    if (extraFormats != null && extraFormats.size() != 0) {
      isSupported = extraFormats.stream().anyMatch(it -> it.equals(format));
    }
    return isSupported;
  }

  @Override
  public ImageApiVersion getImageApiVersion() {
    return new ImageApiVersion(IMAGE_API_VERSION.THREE_POINT_ZERO);
  }
}
