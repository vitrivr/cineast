package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.UnsupportedIIIFAPIException;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION;
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformationV2;
import org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformationV3;

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

  public static ImageRequest fromUrl(String url) throws IOException, UnsupportedIIIFAPIException {
    ImageRequest imageRequest = new ImageRequest();
    url = URLDecoder.decode(url, StandardCharsets.UTF_8);
    String[] split = url.split("/");
    String[] qualityDotFormat = split[split.length - 1].split("\\.");
    // Check if this is a fully qualified IIIF image request or only the base URL\
    if (qualityDotFormat.length == 1 || qualityDotFormat[1].equals("json")) {
      return fromBaseUrl(url);
    }
    imageRequest.setRegion(split[split.length - 4]);
    imageRequest.setSize(split[split.length - 3]);
    imageRequest.setRotation(split[split.length - 2]);
    imageRequest.setQuality(qualityDotFormat[0]);
    imageRequest.setExtension(qualityDotFormat[1]);
    imageRequest.setBaseUrl(String.join("/", Arrays.stream(split).limit(split.length - 4).toList()));
    LOGGER.info("ImageRequest parsed from url: " + imageRequest);
    return imageRequest;
  }

  public static ImageRequest fromUrlIgnoreParams(String url) throws IOException, UnsupportedIIIFAPIException {
    url = URLDecoder.decode(url, StandardCharsets.UTF_8);
    String[] split = url.split("/");
    String[] qualityDotFormat = split[split.length - 1].split("\\.");
    // Check if this is a fully qualified IIIF image request or only the base URL\
    var baseUrl = qualityDotFormat.length == 1 || qualityDotFormat[1].equals("json")
        ? url
        : String.join("/", Arrays.stream(split).limit(split.length - 4).toList());
    return fromBaseUrl(baseUrl);
  }

  public static ImageRequest fromBaseUrl(String baseUrl) throws IOException, UnsupportedIIIFAPIException {
    var info = "/info.json";
    var infoUrl = baseUrl;
    if (baseUrl.endsWith(info)) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - info.length());
    } else {
      infoUrl += info;
    }
    var imageInfoString = getImageInfoJson(infoUrl);
    var apiVersion = parseImageAPIVersion(imageInfoString);
    var imageInformation = switch (apiVersion) {
      case TWO_POINT_ONE_POINT_ONE -> new ObjectMapper().readValue(imageInfoString, ImageInformationV2.class);
      case THREE_POINT_ZERO -> new ObjectMapper().readValue(imageInfoString, ImageInformationV3.class);
    };
    return new ImageRequest()
        .setBaseUrl(baseUrl)
        .setRegion(imageInformation.getMaxRegion())
        .setSize(imageInformation.getMaxSize())
        .setRotation("0")
        .setQuality("default")
        .setExtension("jpg");
  }

  /**
   * Downloads the IIIF image information JSON as string.
   *
   * @param infoURL IIIF image information URL
   * @return the IIIF image information JSON as string
   */
  private static String getImageInfoJson(String infoURL) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(infoURL).openConnection();
    connection.setRequestProperty("accept", "application/json");
    InputStream responseStream = connection.getInputStream();
    return IOUtils.toString(responseStream, StandardCharsets.UTF_8);
  }

  private static IMAGE_API_VERSION parseImageAPIVersion(String imageInfoString) throws JsonProcessingException, UnsupportedIIIFAPIException {
    var mapper = new ObjectMapper();
    var jsonNode = mapper.readTree(imageInfoString);
    var context = jsonNode.get("@context");
    var versionString = context.textValue();

    return ImageApiVersion.parse(versionString);
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
    var FORWARD_SLASH_DELIMITER = "/";
    var parts = new String[]{baseUrl, region, size, rotation, quality + "." + extension};
    return String.join(FORWARD_SLASH_DELIMITER, parts);
  }

  /**
   * Downloads and saves the image to the local filesystem. See {@link ImageRequest#downloadImage(String, String, String)}
   */
  public void downloadImage(String filePath, String fileName) throws IOException {
    downloadImage(filePath, fileName, this.generateIIIFRequestUrl());
  }

  /**
   * Downloads and saves the image to the local filesystem
   *
   * @param filePath   The path of the directory where the image should be saved
   * @param fileName   The name that should be given to the saved image
   * @param requestUrl The complete IIIF Image API compliant URL of the image resource. Useful when URL doesn't need to be generated or has to be overridden.
   * @throws IOException If the image could not be downloaded or written to the filesystem
   */
  public void downloadImage(String filePath, String fileName, String requestUrl) throws IOException {
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
