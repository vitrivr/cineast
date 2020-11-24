package org.vitrivr.cineast.standalone.importer.vbs2019.gvision;

import com.fasterxml.jackson.databind.JsonNode;
import org.vitrivr.cineast.core.data.tag.Preference;

public class GoogleVisionWebTuple {

  public final String labelId;
  public final String description;
  public final float score;
  public final Preference preference;

  public GoogleVisionWebTuple(String labelId, String description, float score, Preference preference) {
    this.labelId = labelId;
    this.description = description;
    this.score = score;
    this.preference = preference;
  }

  public GoogleVisionWebTuple(JsonNode node) {
    this(node.get("id").asText(), node.get("decription").asText(),
        (float) node.get("score").asDouble(), null);
  }
}
