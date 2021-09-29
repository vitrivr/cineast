package org.vitrivr.cineast.core.iiif.discoveryapi.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrderedCollectionPage {

  private static final Logger LOGGER = LogManager.getLogger();

  @JsonProperty("@context")
  private List<String> atContext;

  @JsonProperty
  private String id;

  @JsonProperty
  private String type;

  @JsonProperty
  private Long startIndex;

  @JsonProperty
  private IdTypeObject partOf;

  @JsonProperty
  private IdTypeObject prev;

  @JsonProperty
  private IdTypeObject next;

  @JsonProperty
  private List<OrderedItem> orderedItems;

  public OrderedCollectionPage() {
  }

  public static OrderedCollectionPage fromUrl(String url) throws Exception {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("accept", "application/json");
    InputStream responseStream = connection.getInputStream();
    String collectionPageJson = IOUtils.toString(responseStream, StandardCharsets.UTF_8);
    OrderedCollectionPage orderedCollectionPage = null;
    try {
      orderedCollectionPage = new ObjectMapper().readValue(collectionPageJson, OrderedCollectionPage.class);
    } catch (IOException e) {
      LOGGER.error("Could not parse OrderedCollectionPage from URL: " + url);
      e.printStackTrace();
    }
    return orderedCollectionPage;
  }

  public List<String> getAtContext() {
    return atContext;
  }

  public void setAtContext(List<String> atContext) {
    this.atContext = atContext;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Long getStartIndex() {
    return startIndex;
  }

  public void setStartIndex(Long startIndex) {
    this.startIndex = startIndex;
  }

  public IdTypeObject getPartOf() {
    return partOf;
  }

  public void setPartOf(IdTypeObject partOf) {
    this.partOf = partOf;
  }

  public IdTypeObject getPrev() {
    return prev;
  }

  public void setPrev(IdTypeObject prev) {
    this.prev = prev;
  }

  public IdTypeObject getNext() {
    return next;
  }

  public void setNext(IdTypeObject next) {
    this.next = next;
  }

  public List<OrderedItem> getOrderedItems() {
    return orderedItems;
  }

  public void setOrderedItems(List<OrderedItem> orderedItems) {
    this.orderedItems = orderedItems;
  }

  @Override
  public String toString() {
    return "OrderedCollectionPage{" +
        "atContext=" + atContext +
        ", id='" + id + '\'' +
        ", type='" + type + '\'' +
        ", startIndex=" + startIndex +
        ", partOf=" + partOf +
        ", prev=" + prev +
        ", next=" + next +
        ", orderedItems=" + orderedItems +
        '}';
  }
}
