package org.vitrivr.cineast.core.iiif.presentationapi;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.vitrivr.cineast.core.iiif.UnsupportedIIIFAPIException;
import org.vitrivr.cineast.core.iiif.presentationapi.PresentationApiVersion.PRESENTATION_API_VERSION;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.Manifest_v2;
import org.vitrivr.cineast.core.iiif.presentationapi.v3.Manifest_v3;

public class ManifestRequest {

  private static final Logger LOGGER = LogManager.getLogger();
  private final String manifestJSON;
  private final String url;

  public ManifestRequest(String url) throws IOException {
    this.url = url;
    this.manifestJSON = fetchManifest(url);
  }

  /**
   * @return Received Manifest JSON String
   * @throws IOException if an HTTP connection was not established successfully.
   */
  private String fetchManifest(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("accept", "application/json");
    InputStream responseStream = connection.getInputStream();
    return IOUtils.toString(responseStream, StandardCharsets.UTF_8);
  }

  /**
   * Parses the manifest into a {@link Manifest} object
   *
   * @return {@link Manifest}
   */
  @Nullable
  public Manifest parseManifest() throws UnsupportedIIIFAPIException, JsonProcessingException {
    return parseManifest(this.manifestJSON);
  }

  /**
   * This has been created as a separate function to help with unit testing.
   *
   * @param manifestJSON The JSON response received from the server
   * @return {@link Manifest}
   */
  @Nullable
  public Manifest parseManifest(String manifestJSON) throws UnsupportedIIIFAPIException, JsonProcessingException {
    var apiVersion = parsePresentationAPIVersion(manifestJSON);
    return switch (apiVersion) {
      case TWO_POINT_ONE_POINT_ONE -> new ObjectMapper().readValue(manifestJSON, Manifest_v2.class);
      case THREE_POINT_ZERO -> new ObjectMapper().readValue(manifestJSON, Manifest_v3.class);
    };
  }

  private PRESENTATION_API_VERSION parsePresentationAPIVersion(String manifestJSON) throws JsonProcessingException, UnsupportedIIIFAPIException {
    var mapper = new ObjectMapper();
    var jsonNode = mapper.readTree(manifestJSON);
    var statusNode = jsonNode.get("@context");
    var complianceLevel = statusNode.textValue();

    return PresentationApiVersion.parse(complianceLevel);
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
    bufferedOutputStream.write(manifestJSON.getBytes());
    bufferedOutputStream.close();
    fileOutputStream.close();
    LOGGER.debug("Manifest json response data written to file successfully");
  }

  public String getUrl() {
    return url;
  }

  public String getManifestJSON() {
    return manifestJSON;
  }
}