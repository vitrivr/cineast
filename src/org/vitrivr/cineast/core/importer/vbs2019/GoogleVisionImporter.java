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
import javax.swing.text.html.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.tag.CompleteTag;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.core.importer.vbs2019.gvision.GoogleVisionCategory;
import org.vitrivr.cineast.core.importer.vbs2019.gvision.GoogleVisionTuple;

public class GoogleVisionImporter implements Importer<GoogleVisionTuple> {

  private final JsonParser parser;
  private final ObjectMapper mapper;
  private Iterator<Entry<String, JsonNode>> _segments;
  private Iterator<Entry<String, JsonNode>> _categories;
  private static final Logger LOGGER = LogManager.getLogger();
  private int _movieIDCounter;
  private String _segmentID;
  private final GoogleVisionCategory targetCategory;
  private Iterator<JsonNode> _categoryValues;
  private GoogleVisionCategory _category;

  /**
   * @param targetCategory only tuples of this kind are imported
   */
  public GoogleVisionImporter(Path input, GoogleVisionCategory targetCategory, boolean importTags) throws IOException {
    LOGGER.info("Starting Importer for path {} and category {}", input, targetCategory);
    this.targetCategory = targetCategory;
    mapper = new ObjectMapper();
    parser = mapper.getFactory().createParser(input.toFile());
    _movieIDCounter = 1;
    if (parser.nextToken() == JsonToken.START_ARRAY) {
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        ObjectNode node = mapper.readTree(parser);
        _segments = node.fields();
      }
      if (_segments == null) {
        throw new IOException("Empty file");
      }
    } else {
      throw new IOException("Empty file");
    }
  }

  /**
   * Generate a {@link GoogleVisionTuple} from the current state
   */
  private Optional<GoogleVisionTuple> generateTuple() {
    JsonNode next = _categoryValues.next();
    try {
      return Optional.of(GoogleVisionTuple.of(targetCategory, next, String.format("%05d", _movieIDCounter), _segmentID));
    } catch (UnsupportedOperationException e) {
      LOGGER.trace("Cannot generate tuple for category {} and tuple {}", targetCategory, next);
      return Optional.empty();
    }
  }

  /**
   * Search for the next valid {@link GoogleVisionTuple} in the current segment
   */
  private Optional<GoogleVisionTuple> searchWithinSegment() {
    //First, check if there's still a value left in the current array
    if (_category == targetCategory && _categoryValues.hasNext()) {
      return generateTuple();
    }

    //if not, check if we can get values for the target category in the given segment
    while (_categories != null && _category != targetCategory && _categories.hasNext()) {
      Entry<String, JsonNode> nextCategory = _categories.next();
      _category = GoogleVisionCategory.valueOf(nextCategory.getKey().toUpperCase());
      _categoryValues = nextCategory.getValue().iterator();
    }

    //If we succeeded in the previous loop, we should be at the target category and still have a value left to hand out.
    if (_category == targetCategory && _categoryValues.hasNext()) {
      return generateTuple();
    }
    //else we have nothing in this category
    return Optional.empty();
  }

  private Optional<GoogleVisionTuple> searchWithinMovie() {
    do {
      Optional<GoogleVisionTuple> tuple = searchWithinSegment();
      if (tuple.isPresent()) {
        return tuple;
      }
      //we need to move on to the next segment
      if (!_segments.hasNext()) {
        return Optional.empty();
      }
      Entry<String, JsonNode> nextSegment = _segments.next();
      _segmentID = nextSegment.getKey();
      _categories = nextSegment.getValue().fields();
      //Initialize category values
      Entry<String, JsonNode> nextCategory = _categories.next();
      _category = GoogleVisionCategory.valueOf(nextCategory.getKey().toUpperCase());
      _categoryValues = nextCategory.getValue().iterator();
    } while (_segments.hasNext());
    return Optional.empty();
  }

  private synchronized Optional<GoogleVisionTuple> nextPair() {
    Optional<GoogleVisionTuple> tuple = searchWithinMovie();
    if (tuple.isPresent()) {
      return tuple;
    }

    do {
      //we need to go to the next movie
      _movieIDCounter++;
      if (_movieIDCounter % 1_000 == 0) {
        LOGGER.info("Processed {} movies for category {}", _movieIDCounter, targetCategory);
      }
      try {
        if (parser.nextToken() == JsonToken.START_OBJECT) {
          ObjectNode nextMovie = mapper.readTree(parser);
          if (nextMovie == null) {
            LOGGER.info("File for category {} is done", targetCategory);
            return Optional.empty();
          }
          _segments = nextMovie.fields();
          tuple = searchWithinMovie();
          if (tuple.isPresent()) {
            return tuple;
          }
        } else {
          LOGGER.error("File done");
          return Optional.empty();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } while (true);
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
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(GoogleVisionTuple data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    PrimitiveTypeProvider id = PrimitiveTypeProvider.fromObject("v_" + data.movieID + "_" + data.segmentID);
    Optional<Tag> tag = Optional.empty();
    switch (data.category) {
      case PARTIALLY_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case WEB:
        map.put("id", id);
        map.put("tagid", PrimitiveTypeProvider.fromObject(data.web.get().labelId));
        map.put("score", PrimitiveTypeProvider.fromObject(data.web.get().score));
        try {
          tag = Optional.of(new CompleteTag(data.web.get().labelId, data.web.get().description, data.web.get().description));
        } catch (IllegalArgumentException e) {
          LOGGER.trace("Error while initalizing tag {}", e.getMessage());
        }
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
    return map;
  }
}