package org.vitrivr.cineast.standalone.importer.vbs2019;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

public class MLTFeatureImporter implements Importer<Pair<String,float[]>> {

  private static final Logger LOGGER = LogManager.getLogger();
  private final JsonReader reader;
  private static final Gson gson = new Gson();

  public MLTFeatureImporter(Path input) throws IOException {
    reader = new JsonReader(new FileReader(input.toFile()));
    //begin top-level array containing all elements
    reader.beginArray();
  }

  private synchronized Optional<ImmutablePair<String, float[]>> nextPair() {
    try {
      if (reader.peek() == JsonToken.END_ARRAY) {
        return Optional.empty();
      }
      reader.beginObject();
      reader.nextName();
      String id = reader.nextString();
      reader.nextName();
      float[] arr = gson.fromJson(reader, float[].class);
      reader.endObject();
      return Optional.of(ImmutablePair.of(id, arr));
    } catch (IOException e) {
      LOGGER.error(e);
      return Optional.empty();
    }
  }

  /**
   * @return Pair mapping a segmentID to a List of Descriptions
   */
  @Override
  public Pair<String, float[]> readNext() {
    try {
      Optional<ImmutablePair<String, float[]>> node = nextPair();
      return node.orElse(null);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Pair<String, float[]> data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    String id = data.getLeft().substring(0, data.getLeft().lastIndexOf("_"));
    map.put("id", PrimitiveTypeProvider.fromObject(id));
    map.put("feature", PrimitiveTypeProvider.fromObject(data.getRight()));
    return map;
  }
}
