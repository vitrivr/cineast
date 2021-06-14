package org.vitrivr.cineast.core.iiif.imageapi.v2;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 29.05.21
 */
@ToString
public class ImageInformation_v2 implements ImageInformation {

  private static final Logger LOGGER = LogManager.getLogger();

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

  @Override
  public ImageApiVersion getImageApiVersion() {
    return new ImageApiVersion(IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE);
  }

  @Override
  public boolean isQualitySupported(String quality) {
    boolean isSupported = true;
    try {
      List<ProfileItem> profiles = getProfile().second;
      isSupported = profiles.stream().anyMatch(item -> item.getQualities().stream().anyMatch(q -> q.equals(quality)));
    } catch (NullPointerException e) {
      LOGGER.debug("The server has not advertised the qualities supported by it");
    }
    return isSupported;
  }

  @Override
  public boolean isFormatSupported(String format) {
    boolean isSupported = true;
    try {
      List<ProfileItem> profiles = getProfile().second;
      isSupported = profiles.stream().anyMatch(item -> item.getFormats().stream().anyMatch(q -> q.equals(format)));
    } catch (NullPointerException e) {
      LOGGER.debug("The server has not advertised the formats supported by it");
    }
    return isSupported;
  }

  @Override
  public boolean isFeatureSupported(String feature) {
    List<ProfileItem> profiles = this.getProfile().second;
    return profiles.stream().anyMatch(item -> {
      List<String> supports = item.getSupports();
      if (supports == null) {
        throw new NullPointerException("The server has not advertised the features supported by it");
      }
      return supports.stream().anyMatch(q -> q.equals(feature));
    });
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
    // Size of images may be requested larger than the “full” size. See warning.
    public static final String SUPPORTS_SIZE_ABOVE_FULL = "sizeAboveFull";
    // Size of images may be requested in the form “!w,h”.
    public static final String SUPPORTS_SIZE_BY_CONFINED_WH = "sizeByConfinedWh";
    // Size of images may be requested in the form “w,h”, including sizes that would distort the image.
    public static final String SUPPORTS_SIZE_BY_DISTORTED_WH = "sizeByDistortedWh";
    // Size of images may be requested in the form “,h”.
    public static final String SUPPORTS_SIZE_BY_H = "sizeByH";
    // Size of images may be requested in the form “pct:n”.
    public static final String SUPPORTS_SIZE_BY_PCT = "sizeByPct";
    // Size of images may be requested in the form “w,”.
    public static final String SUPPORTS_SIZE_BY_W = "sizeByW";
    // Size of images may be requested in the form “w,h” where the supplied w and h preserve the aspect ratio.
    public static final String SUPPORTS_SIZE_BY_WH = "sizeByWh";
    @Deprecated
    public static final String SUPPORTS_SIZE_BY_WH_LISTED = "sizeByWhListed";
    @Deprecated
    public static final String SUPPORTS_SIZE_BY_FORCED_WH = "sizeByForcedWh";

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
