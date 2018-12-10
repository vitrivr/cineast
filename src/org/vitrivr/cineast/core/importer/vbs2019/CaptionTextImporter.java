package org.vitrivr.cineast.core.importer.vbs2019;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

public class CaptionTextImporter implements Importer<Triple<String, String, String>> {

  private final Iterator<Entry<String, JsonNode>> videos;
  private static final Logger LOGGER = LogManager.getLogger();
  private Iterator<Entry<String, JsonNode>> currentSegments;
  private Iterator<JsonNode> currentDescriptions;
  private String currentSegmentID;
  private String currentVideoID;

  public CaptionTextImporter(Path input) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonParser parser = mapper.getFactory().createParser(input.toFile());
    if (parser.nextToken() == JsonToken.START_OBJECT) {
      ObjectNode node = mapper.readTree(parser);
      videos = node.fields();
      if (videos == null) {
        throw new IOException("Empty file");
      }
    } else {
      throw new IOException("Empty file");
    }
  }

  private synchronized Optional<Triple<String, String, String>> nextPair() {
    while(currentSegments == null || !currentSegments.hasNext()){
      Entry<String, JsonNode> next = videos.next();
      currentVideoID = next.getKey();
      currentSegments = next.getValue().fields();
    }
    while (currentDescriptions == null || !currentDescriptions.hasNext()) {
      Entry<String, JsonNode> next = currentSegments.next();
      currentSegmentID = next.getKey();
      currentDescriptions = next.getValue().iterator();
    }
    return Optional.of(Triple.of(currentVideoID, currentSegmentID, currentDescriptions.next().asText()));
  }

  /**
   * @return Pair mapping a segmentID to a List of Descriptions
   */
  @Override
  public Triple<String, String, String> readNext() {
    try {
      Optional<Triple<String, String, String>> node = nextPair();
      if (!node.isPresent()) {
        return null;
      }
      return node.get();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Triple<String, String, String> data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    String id = "v_" + data.getLeft() + "_" + data.getMiddle();
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject(id));
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(data.getRight()));
    return map;
  }
}
