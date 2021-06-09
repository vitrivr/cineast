package org.vitrivr.cineast.core.iiif.imageapi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class definition of a single IIIF Image Api Request
 *
 * @author singaltanmay
 * @version 1.0
 * @created 29.05.21
 */
public class ImageRequest {

  private static final Logger LOGGER = LogManager.getLogger();

  private String baseUrl = null;
  private String region = null;
  private String size = null;
  private String rotation = null;
  private String quality = null;
  private String extension = null;

  public ImageRequest() {
  }

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

  public ImageRequest setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public String getRegion() {
    return region;
  }

  public ImageRequest setRegion(String region) {
    this.region = region;
    return this;
  }

  public String getSize() {
    return size;
  }

  public ImageRequest setSize(String size) {
    this.size = size;
    return this;
  }

  public String getRotation() {
    return rotation;
  }

  public ImageRequest setRotation(String rotation) {
    this.rotation = rotation;
    return this;
  }

  public String getQuality() {
    return quality;
  }

  public ImageRequest setQuality(String quality) {
    this.quality = quality;
    return this;
  }

  public String getExtension() {
    return extension;
  }

  public ImageRequest setExtension(String extension) {
    this.extension = extension;
    return this;
  }

  // {scheme}://{server}{/prefix}/{identifier}/{region}/{size}/{rotation}/{quality}.{format}
  public String generateIIIFRequestUrl() {
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
    URL url = new URL(this.generateIIIFRequestUrl());
    BufferedImage img = ImageIO.read(url);
    File file = new File(filePath + "/" + fileName + "." + this.getExtension());
    ImageIO.write(img, this.getExtension(), file);
    LOGGER.debug("Image downloaded and written to file successfully. Image url:\t" + url);
  }

  @Override
  public String toString() {
    return generateIIIFRequestUrl();
  }
}
