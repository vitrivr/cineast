package org.vitrivr.cineast.core.iiif.imageapi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

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
  private final String extension;

  public ImageRequest(String baseUrl, String region, String size, String rotation, String quality, String extension) {
    this.baseUrl = baseUrl;
    this.region = region;
    this.size = size;
    this.rotation = rotation;
    this.quality = quality;
    this.extension = extension;
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

  public String getExtension() {
    return extension;
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
        .append(rotation)
        .append(FORWARD_SLASH_DELIMITER)
        .append(quality)
        .append(".")
        .append(extension);
    return url.toString();
  }

  public void saveToFile(String filePath, String fileName) throws IOException {
    URL url = new URL(this.getUrl());
    BufferedImage img = ImageIO.read(url);
    File file = new File(filePath + fileName + "." + this.getExtension());
    ImageIO.write(img, this.getExtension(), file);
  }

  @Override
  public String toString() {
    return getUrl();
  }
}
