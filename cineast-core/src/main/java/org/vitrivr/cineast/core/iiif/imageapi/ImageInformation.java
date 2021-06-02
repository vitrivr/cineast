package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 29.05.21
 */
public class ImageInformation {

  // TODO fix profile field. It is really weird. The first item will always be a String and the rest will be <ProfileItem>.
  /**
   * A list of profiles, indicated by either a URI or an object describing the features supported. The first entry in the list must be a compliance level URI.
   */
  @JsonProperty(required = true)
  public List<Object> profile;
  /**
   * The context document that describes the semantics of the terms used in the document. This must be the URI: http://iiif.io/api/image/2/context.json for version 2.1 of the IIIF Image API. This document allows the response to be interpreted as RDF, using the JSON-LD serialization.
   */
  @JsonProperty(value = "@context", required = true)
  private String atContext;
  /**
   * The base URI of the image as defined in URI Syntax, including scheme, server, prefix and identifier without a trailing slash.
   */
  @JsonProperty(value = "@id", required = true)
  private String atId;
  /**
   * The type for the Image. If present, the value must be the string iiif:Image.
   */
  @JsonProperty(value = "@type")
  private String atType;
  /**
   * The URI http://iiif.io/api/image which can be used to determine that the document describes an image service which is a version of the IIIF Image API.
   */
  @JsonProperty(required = true)
  private String protocol;
  /**
   * The width in pixels of the full image content, given as an integer.
   */
  @JsonProperty(required = true)
  private String width;
  /**
   * The height in pixels of the full image content, given as an integer.
   */
  @JsonProperty(required = true)
  private String height;
  /**
   * A set of descriptions of the parameters to use to request regions of the image (tiles) that are efficient for the server to deliver. Each description gives a width, optionally a height for non-square tiles, and a set of scale factors at which tiles of those dimensions are available.
   */
  @JsonProperty
  private List<TilesItem> tiles;
  /**
   * A set of height and width pairs the client should use in the size parameter to request complete images at different sizes that the server has available. This may be used to let a client know the sizes that are available when the server does not support requests for arbitrary sizes, or simply as a hint that requesting an image of this size may result in a faster response. A request constructed with the w,h syntax using these sizes must be supported by the server, even if arbitrary width and height are not.
   */
  @JsonProperty
  private List<SizesItem> sizes;

  @Override
  public String toString() {
    return "ImageInformation{" +
        "profile=" + profile +
        ", atContext='" + atContext + '\'' +
        ", atId='" + atId + '\'' +
        ", atType='" + atType + '\'' +
        ", protocol='" + protocol + '\'' +
        ", width='" + width + '\'' +
        ", height='" + height + '\'' +
        ", tiles=" + tiles +
        ", sizes=" + sizes +
        '}';
  }

  /**
   * Inner class used to parse the Image Information JSON response.
   */
  public static class SizesItem {

    @JsonProperty
    public Integer width;
    @JsonProperty
    public Integer height;

    public SizesItem() {
    }

    @Override
    public String toString() {
      return "SizesItem{" +
          "width=" + width +
          ", height=" + height +
          '}';
    }
  }

  /**
   * Inner class used to parse the 0+ indexed item in "profile" array of the Image Information JSON response.
   */
  public static class ProfileItem {

    /**
     * List of operations that the server supports such as rotation, mirroring, regionSquare etc.
     */
    @JsonProperty
    public List<String> supports;
    /**
     * List of qualities made available by the server.
     */
    @JsonProperty
    public List<String> qualities;
    /**
     * List of formats supported by the server
     */
    @JsonProperty
    public List<String> formats;

    public ProfileItem() {
    }

    @Override
    public String toString() {
      return "ProfileItem{" +
          "supports=" + supports +
          ", qualities=" + qualities +
          ", formats=" + formats +
          '}';
    }
  }

  /**
   * Inner class used to parse the Image Information JSON response.
   */
  public static class TilesItem {

    @JsonProperty
    public Integer width;
    @JsonProperty
    public List<Integer> scaleFactors;

    public TilesItem() {
    }

    @Override
    public String toString() {
      return "TilesItem{" +
          "width=" + width +
          ", scaleFactors=" + scaleFactors +
          '}';
    }
  }
}
