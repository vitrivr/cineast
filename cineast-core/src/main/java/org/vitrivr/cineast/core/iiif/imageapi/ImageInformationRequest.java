package org.vitrivr.cineast.core.iiif.imageapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Interface that defines functionality that has to be implemented by ImageInformation objects of all versions of the Image API
 */
public interface ImageInformationRequest {

  /**
   * @return Received Image Information JSON String
   * @throws IOException if an HTTP connection was not established successfully.
   */
  static String fetchImageInformation(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("accept", "application/json");
    InputStream responseStream = connection.getInputStream();
    return IOUtils.toString(responseStream, StandardCharsets.UTF_8);
  }

  /**
   * Parses the JSON response received from the server
   *
   * @return {@link ImageInformation}
   */
  @Nullable
  ImageInformation parseImageInformation();

  /**
   * This has been created as a separate function to help with unit testing.
   *
   * @param response The JSON response received from the server
   * @return {@link ImageInformation}
   */
  @Nullable
  ImageInformation parseImageInformation(String response);

  /**
   * @param filePath The base directory where the file has to be written
   * @param fileName The name of the output file without the extension
   * @throws IOException If the Http request or writing to file fails
   */
  void saveToFile(String filePath, String fileName) throws IOException;
}
