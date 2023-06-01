package org.vitrivr.cineast.core.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractFeatureClientWrapper {
  protected final FeatureClient client;
  protected static final Logger LOGGER = LogManager.getLogger();
  protected AbstractFeatureClientWrapper(FeatureClient client) {
    this.client = client;
  }

  private static FeatureClient buildFeatureClient(Map<String,String> properties){
    if(!properties.containsKey("endpoint")){
      throw new IllegalStateException("No endpoint specified for external client"); //throw better exception
    }
    String endpoint = properties.get("endpoint");
    return new FeatureClient(endpoint);
  }

  private static JsonNode getConfig(Map<String,String> properties) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    if(!properties.containsKey("config_path")){
      LOGGER.warn("No config path specified for external client, using empty config.");
      return mapper.createObjectNode();
    }else {
      return mapper.readTree(new File(properties.get("config_path")));
    }
  }

  public static AbstractFeatureClientWrapper build(Map<String,String> properties) throws Exception {
    FeatureClient client = buildFeatureClient(properties);
    JsonNode config = getConfig(properties);
    switch (properties.getOrDefault("api","simpleFES")){
      case ("simpleFES"):
        return new SimpleFESWrapper(client, config);
      default:
        throw new IllegalStateException("Unexpected value: " + properties.getOrDefault("api", "simpleFES"));
    }
  }


  abstract public String extractTextFromImage(BufferedImage bufImg) throws IOException, InterruptedException;

  abstract public float[] extractVectorFromImage(BufferedImage bufImg) throws IOException, InterruptedException;

  abstract public float[] extractVectorFromText(String text) throws IOException, InterruptedException;

}
