package org.vitrivr.cineast.core.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.util.web.ImageParser;

public class SimpleFESWrapper extends  AbstractFeatureClientWrapper{

  private final JsonMapper mapper = new JsonMapper();
  private final JsonNode config;
  public SimpleFESWrapper(FeatureClient client, JsonNode config) throws Exception {
    super(client);
    this.config = config;
  }

  @Override
  public String extractTextFromImage(BufferedImage bufImg) throws IOException, InterruptedException {
    var dataURL = ImageParser.bufferedImageToDataURL(bufImg, "png");
    // Make a deep copy of the node
    JsonNode copyJsonNode = this.config.deepCopy();

    // Cast the copied JsonNode as an ObjectNode
    ObjectNode objectNode = (ObjectNode) copyJsonNode;

    // Add some properties
    objectNode.put("image", dataURL);

    // Convert back to string and print
    String requestBody = mapper.writeValueAsString(objectNode);

//    Gson gson = new Gson();
//
//    Map<String, Object> map = new HashMap<>();
//    map.put("image", query);
//
//    String body = gson.toJson(map);

    String responseBody = this.client.getResponse(requestBody);
    return mapper.readValue(responseBody,String[].class)[0]; // change this
  }

  @Override
  public float[] extractVectorFromImage(BufferedImage bufImg) throws IOException, InterruptedException {
    return new float[0];
  }

  @Override
  public float[] extractVectorFromText(String text) throws IOException, InterruptedException {
    return new float[0];
  }


}
