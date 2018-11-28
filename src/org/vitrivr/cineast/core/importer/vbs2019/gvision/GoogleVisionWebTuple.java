package org.vitrivr.cineast.core.importer.vbs2019.gvision;

import com.fasterxml.jackson.databind.JsonNode;

public class GoogleVisionWebTuple {

  public final String labelId;
  public final String description;
  public final float score;

  public GoogleVisionWebTuple(String labelId, String description, float score) {
    this.labelId = labelId;
    this.description = description;
    this.score = score;
  }

  public GoogleVisionWebTuple(JsonNode node) {
    this(node.get("id").asText(), node.get("decription").asText(), (float) node.get("score").asDouble());
  }
}
