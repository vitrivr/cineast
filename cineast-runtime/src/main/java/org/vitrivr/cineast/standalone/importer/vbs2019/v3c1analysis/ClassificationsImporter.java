package org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis.ClassificationsImporter.ClassificationTuple;

/**
 * Imports the classifications as given by the https://github.com/klschoef/V3C1Analysis repo. File is expected to be in format:
 *
 * { "movieID": { "segmentID": { "0 based line number in synset.txt": "score / classification confidence",
 */
public class ClassificationsImporter implements Importer<ClassificationTuple> {

  private final Iterator<Entry<String, JsonNode>> movies;
  private static final Logger LOGGER = LogManager.getLogger();
  private final Path input;
  private final List<String> synsetLines;
  private final boolean importSegmentTags;
  private final boolean importTagsFt;
  private Iterator<String> _names;
  private LineIterator lineIterator = null;
  private Iterator<Entry<String, JsonNode>> _tags;
  private String _segmentID;
  private Iterator<Entry<String, JsonNode>> _segments;
  private String _movieID;
  private String _tagId;


  public ClassificationsImporter(Path input, Path synset, boolean importSegmentTags, boolean importTagsFt) throws IOException {
    this.input = input;
    synsetLines = FileUtils.readLines(synset.toFile(), Charset.defaultCharset());
    this.importSegmentTags = importSegmentTags;
    this.importTagsFt = importTagsFt;
    if (importSegmentTags) {
      lineIterator = FileUtils.lineIterator(synset.toFile());
      if (!lineIterator.hasNext()) {
        throw new IOException("Empty synset file");
      }
      String next = lineIterator.next();
      _tagId = next.split(" ")[0];
      String[] names = next.substring(next.indexOf(" ") + 1).split(",");
      _names = Arrays.stream(names).iterator();
      throw new UnsupportedOperationException("There is an inherent mismatch between the tag retrieval logic and the way synsets are structured. Therefore, importing tags into cineast_tags makes no sense");
    }
    if (!importTagsFt) {
      throw new UnsupportedOperationException("There is an inherent mismatch between the tag retrieval logic and the way synsets are structured. Therefore, importing tags into features_segmenttags makes no sense");
    }
    ObjectMapper mapper = new ObjectMapper();
    JsonParser parser = mapper.getFactory().createParser(input.toFile());
    if (parser.nextToken() == JsonToken.START_OBJECT) {
      ObjectNode node = mapper.readTree(parser);
      movies = node.fields();
      if (movies == null || !movies.hasNext()) {
        throw new IOException("Empty file");
      }
      do {
        Entry<String, JsonNode> next = movies.next();
        _segments = next.getValue().fields();
        _movieID = next.getKey();
      } while (!_segments.hasNext());
      Entry<String, JsonNode> nextSegment = _segments.next();
      _segmentID = nextSegment.getKey();
      _tags = nextSegment.getValue().fields();
    } else {
      throw new IOException("Empty file");
    }
  }

  private synchronized Optional<ClassificationTuple> nextPair() {
    if (importSegmentTags) {
      while (!_names.hasNext()) {
        if (!lineIterator.hasNext()) {
          LOGGER.error("Reached end of line iterator");
          return Optional.empty();
        }
        String next = lineIterator.next();
        _tagId = next.split(" ")[0];
        String[] names = next.substring(next.indexOf(" ") + 1).split(",");
        _names = Arrays.stream(names).iterator();
      }
      String name = _names.next();
      //TODO one id corresponds to multiple names. This is not supported in the Tags feature which uses the cineast_tags table
      return Optional.of(new ClassificationTuple(null, _tagId, null, name));
    }
    while (!_tags.hasNext()) {
      while (!_segments.hasNext()) {
        if (!movies.hasNext()) {
          return Optional.empty();
        }
        Entry<String, JsonNode> next = movies.next();
        _segments = next.getValue().fields();
        _movieID = next.getKey();
      }
      Entry<String, JsonNode> nextSegment = _segments.next();
      _segmentID = nextSegment.getKey();
      _tags = nextSegment.getValue().fields();
    }
    Entry<String, JsonNode> _nextTag = _tags.next();
    if (_nextTag.getKey() == null || _nextTag.getKey().equals("")) {
      return nextPair();
    }
    if (_nextTag.getValue() == null || _nextTag.getValue().asText().equals("")) {
      return nextPair();
    }
    try {
      Integer.parseInt(_nextTag.getKey());
    } catch (NumberFormatException e) {
      LOGGER.error("{} is not a number ", _nextTag.getKey(), e);
      return nextPair();
    }
    if (_nextTag.getValue().asDouble() == 0d) {
      LOGGER.error("{} is not a double", _nextTag.getValue().asText());
      return nextPair();
    }
    if (importTagsFt) {
      return Optional.of(new ClassificationTuple(_movieID + "_" + _segmentID, null, null, synsetLines.get(Integer.parseInt(_nextTag.getKey()))));
    }
    //TODO write logic to store tags into features_segmenttags
    return Optional.empty();
  }

  /**
   * @return Pair mapping a segmentID to a List of Descriptions
   */
  @Override
  public ClassificationTuple readNext() {
    try {
      Optional<ClassificationTuple> node = nextPair();
      return node.orElse(null);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(ClassificationTuple data) {
    if (data.tagId != null && data.tag != null) {
      //TODO convert to representation to be stored into cineast_tags
    }
    if (data.score != null) {
      //TODO convert to representation for features_segmenttags
    }
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);

    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject(data.id));
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(data.tag));
    return map;
  }

  static class ClassificationTuple {

    private final String id;
    private final String tagId;
    private final String score;
    private final String tag;

    ClassificationTuple(String id, String tagId, String score, String tag) {

      this.id = id;
      this.tagId = tagId;
      this.score = score;
      this.tag = tag;
    }
  }
}
