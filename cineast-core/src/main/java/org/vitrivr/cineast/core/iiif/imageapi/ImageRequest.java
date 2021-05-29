package org.vitrivr.cineast.core.iiif.imageapi;

/**
 * Class definition of a single IIIF Image Api Request
 *
 * @author singaltanmay
 * @version 1.0
 * @created 29.05.21
 */
public class ImageRequest {

  private final String baseUrl;
  private final String region;
  private final String size;
  private final String rotation;
  private final String quality;

  public ImageRequest(String baseUrl, String region, String size, String rotation, String quality) {
    this.baseUrl = baseUrl;
    this.region = region;
    this.size = size;
    this.rotation = rotation;
    this.quality = quality;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getRegion() {
    return region;
  }

  public String getSize() {
    return size;
  }

  public String getRotation() {
    return rotation;
  }

  public String getQuality() {
    return quality;
  }

  // {scheme}://{server}{/prefix}/{identifier}/{region}/{size}/{rotation}/{quality}.{format}
  public String getUrl() {
    StringBuilder url = new StringBuilder(baseUrl);
    String FORWARD_SLASH_DELIMITER = "/";
    url.append(FORWARD_SLASH_DELIMITER)
        .append(region)
        .append(FORWARD_SLASH_DELIMITER)
        .append(size)
        .append(FORWARD_SLASH_DELIMITER)
        .append(rotation);
    return url.toString();
  }

  @Override
  public String toString() {
    return getUrl();
  }
}
