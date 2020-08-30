package org.vitrivr.cineast.core.util.pose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class PoseSpecs implements Iterable<Entry<String, PoseSpec>> {
  private static PoseSpecs instance = null;
  public HashMap<String, PoseSpec> specs;
  public HashMap<String, String> flips;
  public int[][] kpPairs = null;

  public static PoseSpecs getInstance() {
    if (PoseSpecs.instance == null) {
      PoseSpecs.instance = new PoseSpecs();
    }
    return PoseSpecs.instance;
  }

  private void loadKpPairs(ObjectMapper jsonMapper, JsonNode node) {
    try {
      this.kpPairs = jsonMapper.treeToValue(node, int[][].class);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  private PoseSpecs() {
    ObjectMapper jsonMapper = new ObjectMapper();
    JsonNode root;
    try (InputStream skels = PoseSpec.class.getResourceAsStream("skels.json")) {
      root = jsonMapper.readTree(skels);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    this.specs = new HashMap<>();
    this.flips = new HashMap<>();
    for (Iterator<Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
      Entry<String, JsonNode> kv = it.next();
      String key = kv.getKey();
      if (key.equals("__KP_PAIRS__")) {
        loadKpPairs(jsonMapper, kv.getValue());
      }
      if (key.startsWith("__")) {
        continue;
      }
      JsonNode node = kv.getValue();
      this.specs.put(key, new PoseSpec(node));
      JsonNode flipOther = node.get("flipped");
      if (flipOther != null) {
        this.flips.put(key, flipOther.asText());
      }
    }
  }

  public Iterator<Entry<String, PoseSpec>> iterator() {
    return this.specs.entrySet().iterator();
  }
}
