package org.vitrivr.cineast.core.util.pose;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.jcodec.common.IOUtils;

public class PoseSpecs implements Iterable<Entry<String, PoseSpec>> {
  private static PoseSpecs instance = null;
  public HashMap<String, PoseSpec> specs;

  public static PoseSpecs getInstance() {
    if (PoseSpecs.instance == null) {
      PoseSpecs.instance = new PoseSpecs();
    }
    return PoseSpecs.instance;
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
    for (Iterator<Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
      Entry<String, JsonNode> kv = it.next();
      if (kv.getKey().startsWith("__")) {
        continue;
      }
      this.specs.put(kv.getKey(), new PoseSpec(kv.getValue()));
    }
  }

  public Iterator<Entry<String, PoseSpec>> iterator() {
    return this.specs.entrySet().iterator();
  }
}
