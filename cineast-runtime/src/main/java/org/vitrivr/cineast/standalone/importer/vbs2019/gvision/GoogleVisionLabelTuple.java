package org.vitrivr.cineast.standalone.importer.vbs2019.gvision;

import com.fasterxml.jackson.databind.JsonNode;

public class GoogleVisionLabelTuple {

  public final String labelId;
  public final String description;
  public final float score;

  public GoogleVisionLabelTuple(JsonNode node) {
    this(node.get("id").asText(), node.get("decription").asText(), (float) node.get("score").asDouble());
  }

  public GoogleVisionLabelTuple(String labelId, String description, float score) {
    this.labelId = labelId;
    this.description = description;
    this.score = score;
  }
}
