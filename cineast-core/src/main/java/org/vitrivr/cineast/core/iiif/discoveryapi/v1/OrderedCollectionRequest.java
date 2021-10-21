package org.vitrivr.cineast.core.iiif.discoveryapi.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.vitrivr.cineast.core.iiif.discoveryapi.v1.models.OrderedCollection;

public class OrderedCollectionRequest {

  private static final Logger LOGGER = LogManager.getLogger();
  private final String collectionJSON;
  private final String url;

  public OrderedCollectionRequest(String url) throws IOException {
    this.url = url;
    this.collectionJSON = fetchCollection(url);
  }

  /**
   * @return Received Ordered Collection JSON String
   * @throws IOException if an HTTP connection was not established successfully.
   */
  private String fetchCollection(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("accept", "application/json");
    InputStream responseStream = connection.getInputStream();
    return IOUtils.toString(responseStream, StandardCharsets.UTF_8);
  }


  /**
   * Parses the collection into a {@link OrderedCollection} object
   *
   * @return {@link OrderedCollection}
   */
  @Nullable
  public OrderedCollection parseOrderedCollection() {
    return parseOrderedCollection(this.collectionJSON);
  }

  /**
   * This has been created as a separate function to help with unit testing.
   *
   * @param response The JSON response received from the server
   * @return {@link OrderedCollection}
   */
  @Nullable
  public OrderedCollection parseOrderedCollection(String response) {
    OrderedCollection collection = null;
    if (response == null || response.isEmpty()) {
      response = this.collectionJSON;
    }
    try {
      collection = new ObjectMapper().readValue(response, OrderedCollection.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return collection;
  }

  public String getCollectionJSON() {
    return collectionJSON;
  }

  public String getUrl() {
    return url;
  }
}
