package org.vitrivr.cineast.core.importer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonObjectImporter implements Importer<ObjectNode> {
  
  private final JsonParser parser;
  private final ObjectMapper mapper = new ObjectMapper();
  private final File inputFile;
  private static final Logger LOGGER = LogManager.getLogger();
  private boolean open = false;
  
  public JsonObjectImporter(File input) throws IOException {
    this.inputFile = input;
    this.parser = this.mapper.getFactory().createParser(input);
  }
  
  private synchronized boolean open() throws IOException {
    if (!open) {
      JsonToken token = parser.nextToken();
      if (token != JsonToken.START_ARRAY) {
        if (token == null) {
          return false;
        }
        throw new IllegalStateException("Expected an array for inputFile " + inputFile);
      }
    }
    open = true;
    return true;
  }
  
  @Override
  public ObjectNode readNext() {
    try {
      if (!open) {
        if (!open()) {
          //Trying to read empty document
          return null;
        }
      }
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        return mapper.readTree(parser);
      }
    } catch (IOException e) {
      LOGGER.error("error while reading json file '{}': {}", this.inputFile.getAbsolutePath(), LogHelper.getStackTrace(e));
    }
    return null;
  }
  
  @Override
  public Map<String, PrimitiveTypeProvider> convert(ObjectNode node) {
    @SuppressWarnings("unchecked")
    Map<String, Object> result = mapper.convertValue(node, Map.class);
    Map<String, PrimitiveTypeProvider> map = new HashMap<>(result.size());
    
    for (String key : result.keySet()) {
      Object o = result.get(key);
      map.put(key, PrimitiveTypeProvider.fromObject(o));
    }
    return map;
    
  }
  
}
