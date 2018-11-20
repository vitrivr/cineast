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
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.tag.CompleteTag;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.dao.TagHandler;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.core.util.TimeHelper;

public class GoogleVisionImporter implements Importer<GoogleVisionTuple> {

  private final Iterator<Entry<String, JsonNode>> elements;
  private static final Logger LOGGER = LogManager.getLogger();
  private final String objectID;
  private final String segmentID;
  private final GoogleVisionCategory category;
  private boolean importTags;
  private Iterator<JsonNode> currentTuples;
  private GoogleVisionCategory currentCategory;
  private TagHandler tagHandler;

  /**
   * @param category only tuples of this kind are imported
   */
  public GoogleVisionImporter(Path input, GoogleVisionCategory category, boolean importTags) throws IOException {
    objectID = input.getFileName().toString().substring(input.getFileName().toString().indexOf("_")).replace("shot", "");
    segmentID = input.getFileName().toString().replace("shot" + objectID + "_", "").replace("_RKF.png.json", "");
    this.category = category;
    this.importTags = importTags;
    if (importTags = true) {
      tagHandler = new TagHandler();
    }
    LOGGER.debug("Filename {} mapped to objectID {} and segmentID {}", input.getFileName(), objectID, segmentID);
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

  private synchronized Optional<GoogleVisionTuple> nextPair() {
    while (currentTuples == null || !currentTuples.hasNext() || currentCategory != category) {
      Entry<String, JsonNode> next = elements.next();
      currentCategory = GoogleVisionCategory.valueOf(next.getKey());
      LOGGER.debug("Checking category {}", currentCategory);
      if (currentCategory != category) {
        continue;
      }
      currentTuples = next.getValue().iterator();
    }
    return Optional.of(GoogleVisionTuple.of(category, currentTuples.next()));
  }

  /**
   * @return Pair mapping a segmentID to a List of Descriptions
   */
  @Override
  public GoogleVisionTuple readNext() {
    try {
      Optional<GoogleVisionTuple> node = nextPair();
      if (!node.isPresent()) {
        return null;
      }
      return node.get();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(GoogleVisionTuple data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    PrimitiveTypeProvider id = PrimitiveTypeProvider.fromObject("v_" + objectID + "_" + segmentID);
    Optional<Tag> tag = Optional.empty();
    switch (data.category) {
      case PARTIALLY_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case WEB:
        map.put("id", id);
        map.put("tagid", PrimitiveTypeProvider.fromObject(data.web.get().labelId));
        map.put("score", PrimitiveTypeProvider.fromObject(data.web.get().score));
        tag = Optional.of(new CompleteTag(data.web.get().labelId, data.web.get().description, data.web.get().description));
        break;
      case PAGES_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case FULLY_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case LABELS:
        map.put("id", id);
        map.put("tagid", PrimitiveTypeProvider.fromObject(data.label.get().labelId));
        map.put("score", PrimitiveTypeProvider.fromObject(data.label.get().score));
        tag = Optional.of(new CompleteTag(data.label.get().labelId, data.label.get().description, data.label.get().description));
        break;
      case OCR:
        map.put("id", id);
        map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(data.ocr.get().description));
        // Score is ignored because it's never used in search and 0 anyways
        //map.put("score", PrimitiveTypeProvider.fromObject(data.ocr.get().score));
        break;
      default:
        throw new UnsupportedOperationException();
    }
    LOGGER.debug("Converting {} to {}", data, map);
    if (importTags && tag.isPresent()) {
      if (tagHandler.getTagById(tag.get().getId()) == null) {
        tagHandler.addTag(tag.get());
      }
    }
    return map;
  }
}