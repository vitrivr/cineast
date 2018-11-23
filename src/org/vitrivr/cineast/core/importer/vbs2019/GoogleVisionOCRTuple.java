package org.vitrivr.cineast.core.importer.vbs2019;

import com.fasterxml.jackson.databind.JsonNode;

public class GoogleVisionOCRTuple {

  public final String ocrId;
  public final String description;
  public final float score;

  public GoogleVisionOCRTuple(String ocrId, String description, float score) {
    this.ocrId = ocrId;
    this.description = description;
    this.score = score;
  }

  public GoogleVisionOCRTuple(JsonNode node) {
    this(node.get("id").asText(), node.get("decription").asText(), (float) node.get("score").asDouble());
  }
}


