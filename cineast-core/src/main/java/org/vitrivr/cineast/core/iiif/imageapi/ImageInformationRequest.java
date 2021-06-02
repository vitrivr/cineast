package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 02.06.21
 */
public class ImageInformationRequest {

  /**
   * @param url The Image Information request URL used to make a request to the IIIF server.
   * @return Received Image Information deserialized into {@link ImageInformation}
   * @throws IOException if an HTTP connection was not established successfully.
   */
  public static ImageInformation fetchImageInformation(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("accept", "application/json");
    InputStream responseStream = connection.getInputStream();
    String response = IOUtils.toString(responseStream, StandardCharsets.UTF_8);
    return parseImageInformationJson(response);
  }

  /**
   * This has been created as a separate function to help with unit testing.
   * @param response The JSON response received from the server
   * @return {@link ImageInformation}
   */
  @Nullable
  public static ImageInformation parseImageInformationJson(String response) {
    ImageInformation imageInformation = null;
    try {
      imageInformation = new ObjectMapper().readValue(response, ImageInformation.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imageInformation;
  }
}
