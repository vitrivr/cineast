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
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2;
import org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3;

public class ImageFetcher {

  public static void fetch(IIIFItem item, String directory) throws IOException, UnsupportedIIIFAPIException {
    var imageInfoString = getImageInfo(item.identifier);
    var apiVersion = parseImageAPIVersion(imageInfoString);
    var imageInformation = switch (apiVersion) {
      case TWO_POINT_ONE_POINT_ONE -> new ObjectMapper().readValue(imageInfoString, ImageInformation_v2.class);
      case THREE_POINT_ZERO -> new ObjectMapper().readValue(imageInfoString, ImageInformation_v3.class);
    };
    var imageRequest = new ImageRequest()
        .setBaseUrl(item.identifier.substring(0, item.identifier.length() - "/info.json".length()))
        .setRegion(imageInformation.getMaxRegion())
        .setSize(imageInformation.getMaxSize())
        .setRotation("0")
        .setQuality("default")
        .setExtension("jpg");
    imageRequest.downloadImage(directory, FilenameUtils.getBaseName(imageInformation.getId()));
  }

  private static String getImageInfo(String infoURL) throws IOException {
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
