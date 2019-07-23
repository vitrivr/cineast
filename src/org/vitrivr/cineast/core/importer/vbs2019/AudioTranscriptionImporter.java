package org.vitrivr.cineast.core.importer.vbs2019;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.StreamTokenizer;
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
import org.vitrivr.cineast.core.importer.Importer;

public class AudioTranscriptionImporter implements Importer<Pair<String, String>> {

  private static final Logger LOGGER = LogManager.getLogger();
  private final JsonParser parser;
  private final ObjectMapper mapper;
  private Iterator<Entry<String, JsonNode>> elements;

  public AudioTranscriptionImporter(Path input) throws IOException {
    mapper = new ObjectMapper();
    parser = mapper.getFactory().createParser(input.toFile());
    if (parser.nextToken() == JsonToken.START_ARRAY) {
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        ObjectNode node = mapper.readTree(parser);
        elements = node.fields();
      }
      if (elements == null) {
        throw new IOException("Empty file");
      }
    } else {
      throw new IOException("Empty file");
    }
  }

  private synchronized Optional<Pair<String, String>> nextPair() {
    if (!elements.hasNext()) {
      do {
        try {
          if (parser.nextToken() == JsonToken.START_OBJECT) {
            ObjectNode nextMovie = mapper.readTree(parser);
            if (nextMovie == null) {
              LOGGER.info("Audiofile is done");
              return Optional.empty();
            }
            elements = nextMovie.fields();
          } else {
            LOGGER.error("File done");
            return Optional.empty();
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } while (!elements.hasNext());
    }
    Entry<String, JsonNode> next = elements.next();
    return Optional.of(new Pair<>(next.getKey(), next.getValue().asText()));
  }

  /**
   * @return Pair mapping a segmentID to a List of Descriptions
   */
  @Override
  public Pair<String, String> readNext() {
    Optional<Pair<String, String>> node = nextPair();
    return node.orElse(null);
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Pair<String, String> data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject(data.first));
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(data.second));
    return map;
  }
}
