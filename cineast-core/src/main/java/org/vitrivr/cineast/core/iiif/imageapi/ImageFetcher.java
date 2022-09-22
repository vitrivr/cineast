package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.vitrivr.cineast.core.iiif.IIIFItem;
import org.vitrivr.cineast.core.iiif.UnsupportedIIIFAPIException;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION;
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformationV2;
import org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformationV3;

public class ImageFetcher {

  public static void fetch(IIIFItem item, String directory) throws IOException, UnsupportedIIIFAPIException {
    var imageRequest = prepareImageRequest(item);
    fetch(imageRequest, directory);
  }

  public static void fetch(ImageRequest imageRequest, String directory) throws IOException {
    imageRequest.downloadImage(directory, FilenameUtils.getBaseName(imageRequest.getBaseUrl()));
  }



  public static ImageRequest prepareImageRequest(IIIFItem item) throws IOException, UnsupportedIIIFAPIException {
    var imageInfoString = getImageInfoJson(item.identifier);
    var apiVersion = parseImageAPIVersion(imageInfoString);
    var imageInformation = switch (apiVersion) {
      case TWO_POINT_ONE_POINT_ONE -> new ObjectMapper().readValue(imageInfoString, ImageInformationV2.class);
      case THREE_POINT_ZERO -> new ObjectMapper().readValue(imageInfoString, ImageInformationV3.class);
    };
    var info = "/info.json";
    var identifier = item.identifier.endsWith(info)
        ? item.identifier.substring(0, item.identifier.length() - info.length())
        : item.identifier;
    return new ImageRequest()
        .setBaseUrl(identifier)
        .setRegion(item.region == null ? imageInformation.getMaxRegion() : item.region)
        .setSize(item.size == null ? imageInformation.getMaxSize() : item.size)
        .setRotation(item.rotation == null ? "0" : item.rotation.toString())
        .setQuality(item.quality == null ? "default" : item.quality)
        .setExtension(item.format == null ? "jpg" : item.format);
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
}
