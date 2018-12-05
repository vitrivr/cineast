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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.dao.TagHandler;
import org.vitrivr.cineast.core.importer.Importer;

public class TagImporter implements Importer<Pair<String, String>> {

  private final Iterator<Entry<String, JsonNode>> elements;
  private static final Logger LOGGER = LogManager.getLogger();

  public TagImporter(Path input) throws IOException {
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
    while (elements.hasNext()) {
      Entry<String, JsonNode> next = elements.next();
      if (next.getValue().asText() == null || next.getValue().asText().equals("")) {
        continue;
      }
      return Optional.of(new Pair<>(next.getKey(), next.getValue().asText()));
    }
    return Optional.empty();
  }

  /**
   * @return Pair mapping a segmentID to a List of Descriptions
   */
  @Override
  public Pair<String, String> readNext() {
    try {
      Optional<Pair<String, String>> node = nextPair();
      return node.orElse(null);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Pair<String, String> data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    map.put(TagHandler.TAG_ID_COLUMNNAME, PrimitiveTypeProvider.fromObject(data.first));
    map.put(TagHandler.TAG_NAME_COLUMNNAME, PrimitiveTypeProvider.fromObject(data.second));
    map.put(TagHandler.TAG_DESCRIPTION_COLUMNNAME, PrimitiveTypeProvider.fromObject(data.second));
    return map;
  }
}
