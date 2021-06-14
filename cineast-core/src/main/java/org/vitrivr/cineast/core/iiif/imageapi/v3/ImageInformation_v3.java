package org.vitrivr.cineast.core.iiif.imageapi.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
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

  /**
   * The width in pixels of the full image content, given as an integer.
   */
  @Getter
  @Setter
  @JsonProperty(required = true)
  private Integer width;
  /**
   * The height in pixels of the full image content, given as an integer.
   */
  @Getter
  @Setter
  @JsonProperty(required = true)
  private Integer height;

  /** A string indicating the highest compliance level which is fully supported by the service. The value must be one of level0, level1, or level2. */
  @Getter
  @Setter
  @JsonProperty(required = true)
  private String profile;

  /**
   * TThe @context tells Linked Data processors how to interpret the image information. If extensions are used then their context definitions should be included in this top-level @context property.
   */
  @Getter
  @Setter
  @JsonProperty(value = "@context", required = true)
  private String atContext;

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
