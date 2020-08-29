package org.vitrivr.cineast.core.util.pose;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;


public class JsonPreEmbeddingReader implements PreEmbeddingReaderInterface {
  /** This class exists to work around https://github.com/Unidata/netcdf-java/issues/460 */
  final private ObjectMapper mapper = new ObjectMapper();
  final private JsonParser jsonParser;

  public JsonPreEmbeddingReader(Path path) {
    JsonFactory jsonFactory = new JsonFactory();
    try {
      this.jsonParser = jsonFactory.createParser(path.toFile());
      if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
        throw new IllegalStateException("Expected content to be an array");
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Optional<ImmutablePair<Integer, float[][]>> readNext() {
    try {
      JsonToken jsonToken = jsonParser.nextToken();
      if (jsonToken == JsonToken.END_ARRAY) {
        return Optional.empty();
      }
      if (jsonToken != JsonToken.START_ARRAY) {
        throw new IllegalStateException("Expected start pair");
      }
      jsonParser.nextToken();
      Integer frameNum = this.mapper.readValue(this.jsonParser, Integer.class);
      float[][] mat = this.mapper.readValue(this.jsonParser, float[][].class);
      if (jsonParser.nextToken() != JsonToken.END_ARRAY) {
        throw new IllegalStateException("Expected end pair");
      }
      return Optional.of(new ImmutablePair<>(frameNum, mat));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Iterator<ImmutablePair<Integer, float[][]>> frameIterator() {
    return Stream.generate(() -> 0).map(x -> readNext()).takeWhile(Optional::isPresent).map(Optional::get).iterator();
  }

  @Override
  public void close() {
    try {
      this.jsonParser.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
