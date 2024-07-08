package org.vitrivr.cineast.core.features;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.exporter.AudioSegmentExporter;
import org.vitrivr.cineast.core.util.web.ImageParser;
import org.vitrivr.cineast.core.util.web.WebClient;
import org.vitrivr.cineast.core.util.web.MessageTemplate;

public class FeatureClient {
  protected final WebClient client;
  protected final FeatureClientRequest request;
  protected final MessageTemplate responseTemplate;

  protected static final Logger LOGGER = LogManager.getLogger();

  public static FeatureClient build(Map<String,String> properties) throws IOException {
    if(!properties.containsKey("endpoint")){
      throw new IllegalArgumentException("No endpoint specified for external client");
    }
    String endpoint = properties.get("endpoint");
    if(!properties.containsKey("request")){
      throw new IllegalArgumentException("No request specified for external client");
    }
    String requestString = properties.get("request");
    if(!properties.containsKey("response")){
      throw new IllegalArgumentException("No response template specified for external client");
    }
    String responseString = properties.get("response");
    FeatureClientRequest request;
    if (new File(requestString).exists()) {
      request = new FeatureClientJsonRequest(requestString);
    }else {
      request = new FeatureClientBinaryRequest(requestString);
    }
    return new FeatureClient(endpoint, request, responseString);
  }

  protected FeatureClient(String endpoint, FeatureClientRequest req, String responseTemplatePath) throws IOException {
    this.client = new WebClient(endpoint);
    this.request = req;
    this.responseTemplate = new MessageTemplate(responseTemplatePath);
  }

  public Map<String,Object> extract(SegmentContainer sc) throws IOException, InterruptedException {

    String response = this.request.execute(sc, this.client);

    Map<String, String> responseKeys = this.responseTemplate.parseString(response);

    HashMap<String, Object> result = new HashMap<>();
    for (var key : responseKeys.keySet()) {
      switch (key) {
        case "raw_text":
          result.put(key, responseKeys.get(key));
          break;
        default:
          LOGGER.warn("Unknown key: {}", key);
          break;
      }
    }
    return result;
  }



}
