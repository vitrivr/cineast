package org.vitrivr.cineast.core.iiif.imageapi;

/**
 * Class definition of a single IIIF Image Api Request
 *
 * @author singaltanmay
 * @version 1.0
 * @created 29.05.21
 */
public class ImageRequest {

  private String baseUrl;

  private String region;

  private String size;

  private String rotation;

  public String getRotation() {
    return rotation;
  }

  public void setRotation(String rotation) {
    this.rotation = rotation;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
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
