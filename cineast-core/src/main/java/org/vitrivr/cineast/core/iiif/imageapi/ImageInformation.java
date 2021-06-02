package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.vitrivr.cineast.core.data.Pair;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 29.05.21
 */
@ToString
public class ImageInformation {

  /**
   * A list of profiles, indicated by either a URI or an object describing the features supported. The first entry in the list must be a compliance level URI.
   */
  @Setter
  @JsonProperty(required = true)
  private List<Object> profile;
  /**
   * The context document that describes the semantics of the terms used in the document. This must be the URI: http://iiif.io/api/image/2/context.json for version 2.1 of the IIIF Image API. This document allows the response to be interpreted as RDF, using the JSON-LD serialization.
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
  @JsonProperty(value = "@id", required = true)
  private String atId;
  /**
   * The type for the Image. If present, the value must be the string iiif:Image.
   */
  @Getter
  @Setter
  @JsonProperty(value = "@type")
  private String atType;
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
  /**
   * A set of descriptions of the parameters to use to request regions of the image (tiles) that are efficient for the server to deliver. Each description gives a width, optionally a height for non-square tiles, and a set of scale factors at which tiles of those dimensions are available.
   */
  @Getter
  @Setter
  @JsonProperty
  private List<TilesItem> tiles;
  /**
   * A set of height and width pairs the client should use in the size parameter to request complete images at different sizes that the server has available. This may be used to let a client know the sizes that are available when the server does not support requests for arbitrary sizes, or simply as a hint that requesting an image of this size may result in a faster response. A request constructed with the w,h syntax using these sizes must be supported by the server, even if arbitrary width and height are not.
   */
  @Getter
  @Setter
  @JsonProperty
  private List<SizesItem> sizes;

  /**
   * Custom getter for getProfile that converts List<Object> into a Pair<String, List<ProfileItem>>
   */
  public Pair<String, List<ProfileItem>> getProfile() {
    final String apiLevelString = this.profile.size() < 1 ? null : ((String) this.profile.get(0));
    final List<ProfileItem> profileItemList = new LinkedList<>();
    for (int i = 1; i < this.profile.size(); i++) {
      @SuppressWarnings("unchecked") final LinkedHashMap<String, ArrayList<String>> map = (LinkedHashMap<String, ArrayList<String>>) this.profile.get(i);
      final ProfileItem profileItem = new ProfileItem();
      profileItem.setSupports(map.getOrDefault("supports", new ArrayList<>()));
      profileItem.setQualities(map.getOrDefault("qualities", new ArrayList<>()));
      profileItem.setFormats(map.getOrDefault("formats", new ArrayList<>()));
      profileItemList.add(profileItem);
    }
    return new Pair<>(apiLevelString, profileItemList);
  }

  /**
   * Inner class used to parse the Image Information JSON response.
   */
  @NoArgsConstructor
  @ToString
  @EqualsAndHashCode
  public static class SizesItem {

    @Getter
    @Setter
    @JsonProperty
    public Integer width;

    @Getter
    @Setter
    @JsonProperty
    public Integer height;

    public SizesItem(int width, int height) {
      this.width = width;
      this.height = height;
    }
  }

  /**
   * Inner class used to parse the 0+ indexed item in "profile" array of the Image Information JSON response.
   */
  @NoArgsConstructor
  @ToString
  public static class ProfileItem {

    /**
     * List of operations that the server supports such as rotation, mirroring, regionSquare etc.
     */
    @Getter
    @Setter
    @JsonProperty
    public List<String> supports;

    /**
     * List of qualities made available by the server.
     */
    @Getter
    @Setter
    @JsonProperty
    public List<String> qualities;

    /**
     * List of formats supported by the server
     */
    @Getter
    @Setter
    @JsonProperty
    public List<String> formats;
  }

  /**
   * Inner class used to parse the Image Information JSON response.
   */
  @NoArgsConstructor
  @ToString
  public static class TilesItem {

    @Getter
    @Setter
    @JsonProperty
    public Integer width;

    @Getter
    @Setter
    @JsonProperty
    public List<Integer> scaleFactors;
  }
}
