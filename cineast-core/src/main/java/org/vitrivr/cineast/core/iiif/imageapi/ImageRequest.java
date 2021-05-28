package org.vitrivr.cineast.core.iiif.imageapi;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 28.05.21
 */
public class ImageRequest {

  private String baseUrl;

  private String region;

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

  // {scheme}://{server}{/prefix}/{identifier}/{region}/{size}/{rotation}/{quality}.{format}
  public String getUrl() {
    StringBuilder url = new StringBuilder(baseUrl);
    url.append("/")
        .append(region);
    return url.toString();
  }

  @Override
  public String toString() {
    return getUrl();
  }
}
