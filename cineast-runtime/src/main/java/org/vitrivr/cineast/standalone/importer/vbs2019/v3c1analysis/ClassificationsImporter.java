package org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis.ClassificationsImporter.ClassificationTuple;

/**
 * Imports the classifications as given by the https://github.com/klschoef/V3C1Analysis repo. File is expected to be in format:
 *
 * { "movieID": { "segmentID": { "0 based line number in synset.txt": "score / classification confidence",
 */
public class ClassificationsImporter implements Importer<ClassificationTuple> {

  private static final Logger LOGGER = LogManager.getLogger();
  private final boolean importSegmentTags;
  private final boolean importTagsFt;
  private final List<String> synsetLines;
  private final JsonReader reader;
  private final Path input;
  private String _segmentID;
  private String _movieID;


  /**
   * Only importTagsFt is currently supported
   */
  public ClassificationsImporter(Path input, List<String> synsetLines, boolean importSegmentTags, boolean importTagsFt) throws IOException {
    this.input = input;
    this.importSegmentTags = importSegmentTags;
    this.importTagsFt = importTagsFt;
    this.synsetLines = synsetLines;
    if (importSegmentTags) {
      throw new UnsupportedOperationException("There is an inherent mismatch between the tag retrieval logic and the way synsets are structured. Therefore, importing tags into cineast_tags makes no sense");
    }
    if (!importTagsFt) {
      throw new UnsupportedOperationException("There is an inherent mismatch between the tag retrieval logic and the way synsets are structured. Therefore, importing tags into features_segmenttags makes no sense");
    }
    reader = new JsonReader(new FileReader(input.toFile()));
    //begin top-level object containing all movies
    reader.beginObject();
    /* move to first item */
    nextMovie();
    nextSegment();
    //now we should be at our starting position: inside a movie, inside a segment and the next token should be a classification
  }

  /**
   * Up next should be "movieID": { or } closing the entire document
   */
  private boolean nextMovie() throws IOException {
    //check if the document has come to its end
    if (reader.peek() == JsonToken.END_OBJECT) {
      LOGGER.info("Closing entire document");
      reader.endObject();
      return false;
    }
    //verify that a name (which we expect to be the movieID is next
    if (reader.peek() != JsonToken.NAME) {
      throw new IOException("Ill-formatted JSON, found " + reader.peek());
    }
    //store current movieID
    _movieID = reader.nextName();
    //begin movie
    if (reader.peek() != JsonToken.BEGIN_OBJECT) {
      throw new IOException("Ill-formatted JSON, found " + reader.peek());
    }
    LOGGER.trace("Starting movie {}", _movieID);
    reader.beginObject();
    //verify that a name (which we expect to be a segmentID is next
    if (reader.peek() != JsonToken.NAME) {
      throw new IOException("Ill-formatted JSON, found " + reader.peek());
    }
    return true;
  }

  /**
   * Either the next item is "segmentID": { or a } closing the movie
   */
  private boolean nextSegment() throws IOException {
    //verify that a name (which we expect to be a segmentID is next
    if (reader.peek() != JsonToken.NAME) {
      //no next segment anymore, so we close this movie and go to the next one
      LOGGER.trace("movie {} done, going to next movie", _movieID);
      reader.endObject();
      //if there is a next movie, look for a segment
      if (nextMovie()) {
        LOGGER.trace("Looking for next segment in movie {}", _movieID);
        return nextSegment();
      }
      LOGGER.trace("no next movie exists, returning false");
      return false;
    }
    _segmentID = reader.nextName();
    //begin segment
    if (reader.peek() != JsonToken.BEGIN_OBJECT) {
      throw new IOException("Ill-formatted JSON, found " + reader.peek());
    }
    LOGGER.trace("Beginning segment {}_{}", _movieID, _segmentID);
    reader.beginObject();
    //verify that an item is next
    if (reader.peek() != JsonToken.NAME) {
      LOGGER.warn("Empty segment {}_{}, going to next segment", _movieID, _segmentID);
      reader.endObject();
      return nextSegment();
    }
    return true;
  }

  /**
   * Expects the next token to be a name, which is the synset-line number
   */
  private Optional<ClassificationTuple> nextTuple() throws IOException {
    String synsetLineNumber = reader.nextName();
    String score = reader.nextString();
    //LOGGER.trace("inserting tuple {}:{} for {}_{}", synsetLineNumber, score, _movieID, _segmentID);
    try {
      Double.parseDouble(score);
      Integer.parseInt(synsetLineNumber);
    } catch (NumberFormatException e) {
      LOGGER.error("{} or {} is not a number ", synsetLineNumber, score, e);
      throw new RuntimeException();
    }
    return Optional.of(new ClassificationTuple("v_" + _movieID + "_" + _segmentID, null, null, synsetLines.get(Integer.parseInt(synsetLineNumber)).split(" ")[0]));
  }

  private synchronized Optional<ClassificationTuple> nextPair() throws IOException {
    if (importSegmentTags) {
      throw new UnsupportedOperationException();
    }
    //check if there is a next classification to score mapping
    if (reader.peek() == JsonToken.NAME) {
      return nextTuple();
    }
    LOGGER.trace("No next tuple found in segment {}_{}, going to next segment", _movieID, _segmentID);
    reader.endObject();
    if (nextSegment()) {
      return nextTuple();
    }
    LOGGER.info("No next segment found, file {} ended", input.getFileName().toString());
    return Optional.empty();
  }

  @Override
  public ClassificationTuple readNext() {
    try {
      Optional<ClassificationTuple> node = nextPair();
      return node.orElse(null);
    } catch (NoSuchElementException | IOException e) {
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

    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], new StringTypeProvider(data.id));
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], new StringTypeProvider(data.tag));
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
