package org.vitrivr.cineast.core.iiif.imageapi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class definition of a single IIIF Image API Request
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

  public static ImageRequest fromUrl(String url) {
    ImageRequest imageRequest = new ImageRequest();
    String[] split = url.split("/");
    imageRequest.setRegion(split[split.length - 4]);
    imageRequest.setSize(split[split.length - 3]);
    imageRequest.setRotation(split[split.length - 2]);
    String[] qualityDotFormat = split[split.length - 1].split("\\.");
    imageRequest.setQuality(qualityDotFormat[0]);
    imageRequest.setExtension(qualityDotFormat[1]);
    StringBuilder baseUrl = new StringBuilder();
    for (int i = 0; i < split.length - 4; i++) {
      baseUrl.append(split[i]).append("/");
    }
    imageRequest.setBaseUrl(baseUrl.toString());
    LOGGER.info("ImageRequest parsed from url: " + imageRequest);
    return imageRequest;
  }

  /**
   * Percent encodes "/","?","#","[","]","@" and "%" to their corresponding ASCII reserved characters
   */
  public static String percentEncode(String toEncode) {
    HashMap<Character, String> mapping = new HashMap<>();
    mapping.put('/', "%2F");
    mapping.put('?', "%3F");
    mapping.put('#', "%23");
    mapping.put('[', "%5B");
    mapping.put(']', "%5D");
    mapping.put('@', "%40");
    mapping.put('%', "%25");
    if (toEncode == null) {
      return null;
    }
    StringBuilder encoded = new StringBuilder(toEncode.length());
    for (Character c : toEncode.toCharArray()) {
      encoded.append(mapping.getOrDefault(c, c.toString()));
    }
    return encoded.toString();
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

  /**
   * Generates an IIIF Image Request URL of the ordering {scheme}://{server}{/prefix}/{identifier}/{region}/{size}/{rotation}/{quality}.{format} where each parameter is percent escaped
   */
  public String generateIIIFRequestUrl() {
    String FORWARD_SLASH_DELIMITER = "/";
    return baseUrl + FORWARD_SLASH_DELIMITER
        + percentEncode(region)
        + FORWARD_SLASH_DELIMITER
        + percentEncode(size)
        + FORWARD_SLASH_DELIMITER
        + percentEncode(rotation)
        + FORWARD_SLASH_DELIMITER
        + percentEncode(quality)
        + "."
        + percentEncode(extension);
  }

  public void saveToFile(String filePath, String fileName) throws IOException {
    saveToFile(filePath, fileName, this.generateIIIFRequestUrl());
  }

  public void saveToFile(String filePath, String fileName, String requestUrl) throws IOException {
    URL url = new URL(requestUrl);
    BufferedImage img = ImageIO.read(url);
    File file = new File(filePath + "/" + fileName + "." + this.getExtension());
    ImageIO.write(img, this.getExtension(), file);
    LOGGER.debug("Image downloaded and written to file successfully. Image url:\t{}", url);
  }

  @Override
  public String toString() {
    return "ImageRequest{" +
        "baseUrl='" + baseUrl + '\'' +
        ", region='" + region + '\'' +
        ", size='" + size + '\'' +
        ", rotation='" + rotation + '\'' +
        ", quality='" + quality + '\'' +
        ", extension='" + extension + '\'' +
        '}';
  }
}
