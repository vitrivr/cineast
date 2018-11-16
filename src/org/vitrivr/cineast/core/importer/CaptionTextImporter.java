package org.vitrivr.cineast.core.importer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public class CaptionTextImporter implements Importer<Pair<String, String>> {

  private final String objectID;
  private final Iterator<Entry<String, JsonNode>> elements;
  private static final Logger LOGGER = LogManager.getLogger();
  private Iterator<JsonNode> currentDescriptions;
  private String currentSegmentID;

  public CaptionTextImporter(Path input) throws IOException {
    objectID = input.getFileName().toString().replace(".json", "");
    ObjectMapper mapper = new ObjectMapper();
    JsonParser parser = mapper.getFactory().createParser(input.toFile());
    if (parser.nextToken() == JsonToken.START_OBJECT) {
      ObjectNode node = mapper.readTree(parser);
      elements = node.fields();
      if (elements == null) {
        throw new IOException("Empty file");
      }
    } else {
      throw new IOException("Empty file");
    }
  }

  private synchronized Optional<Pair<String, String>> nextPair() {
    while (currentDescriptions == null || !currentDescriptions.hasNext()) {
      Entry<String, JsonNode> next = elements.next();
      currentSegmentID = next.getKey();
      currentDescriptions = next.getValue().iterator();
      return Optional.of(new Pair<>(currentSegmentID, currentDescriptions.next().asText()));
    }
    return Optional.of(new Pair<>(currentSegmentID, currentDescriptions.next().asText()));
  }

  /**
   * @return Pair mapping a segmentID to a List of Descriptions
   */
  @Override
  public Pair<String, String> readNext() {
    try {
      Optional<Pair<String, String>> node = nextPair();
      if (!node.isPresent()) {
        return null;
      }
      return node.get();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Pair<String, String> data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    String id = "v_" + objectID + "_" + data.first;
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0],
        PrimitiveTypeProvider.fromObject(id));
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1],
        PrimitiveTypeProvider.fromObject(data.second));
    LOGGER.debug("Converting to {}:{}", id, data.second);
    return map;
  }
}
