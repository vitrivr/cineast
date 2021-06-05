package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 02.06.21
 */
public class ImageInformationRequest {

  private static final Logger LOGGER = LogManager.getLogger();

  private final String url;
  private final String imageInformation;

  public ImageInformationRequest(String url) throws IOException {
    this.url = url;
    this.imageInformation = this.fetchImageInformation();
  }

  /**
   * This has been created as a separate function to help with unit testing.
   *
   * @param response The JSON response received from the server
   * @return {@link ImageInformation}
   */
  @Nullable
  public ImageInformation getImageInformation(String response) {
    ImageInformation imageInformation = null;
    if(response == null || response.isEmpty()) response = this.imageInformation;
    try {
      imageInformation = new ObjectMapper().readValue(response, ImageInformation.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imageInformation;
  }

  /**
   * @param filePath The base directory where the file has to be written
   * @param fileName The name of the output file without the extension
   * @throws IOException If the Http request or writing to file fails
   */
  public void saveToFile(String filePath, String fileName) throws IOException {
    File file = new File(filePath + "/" + fileName + ".json");
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
    //write byte array to file
    bufferedOutputStream.write(imageInformation.getBytes());
    bufferedOutputStream.close();
    fileOutputStream.close();
    LOGGER.debug("Image information request's json response data written to file successfully. Request url:\t" + url);
  }

  /**
   * @return Received Image Information JSON String
   * @throws IOException if an HTTP connection was not established successfully.
   */
  public String fetchImageInformation() throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("accept", "application/json");
    InputStream responseStream = connection.getInputStream();
    return IOUtils.toString(responseStream, StandardCharsets.UTF_8);
  }
}
