package org.vitrivr.cineast.standalone.importer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LIREImporter implements Importer<StringFloatArrayPair> {

  private final BufferedReader reader;
  private final File file;

  private static final Logger LOGGER = LogManager.getLogger();

  public LIREImporter(File file) throws IOException {
    this.file = file;
    this.reader = new BufferedReader(new FileReader(file));

    // skip first two lines
    this.reader.readLine();
    this.reader.readLine();
  }

  @Override
  public StringFloatArrayPair readNext() {
    String line;
    try {
      line = reader.readLine();
      if (line == null || line.trim().isEmpty()) {
        return null;
      }
      String[] split = line.split("\t");

      if (split.length < 4) {
        LOGGER.error("line in '{}' does not conform to expected format",
            this.file.getAbsolutePath());
        return null;
      }

      String id = split[0];

      int vectorLength = 0;
      try {
        vectorLength = Integer.parseInt(split[2]);
      } catch (NumberFormatException e) {
        LOGGER.error("'{}' is not an integer in '{}'", split[2], this.file.getAbsolutePath());
        return null;
      }

      float[] vector = new float[vectorLength];
      String[] stringVector = split[3].split(" ");

      for (int i = 0; i < Math.min(vectorLength, stringVector.length); ++i) {
        try {
          vector[i] = Float.parseFloat(stringVector[i]);
        } catch (NumberFormatException e) {
          LOGGER.error("'{}' is not a float in '{}'", stringVector[i], this.file.getAbsolutePath());
        }
      }

      return new StringFloatArrayPair(id, vector);

    } catch (IOException e) {

      e.printStackTrace();
      return null;
    }

  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(StringFloatArrayPair data) {
    HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    map.put("id", new StringTypeProvider(data.id));
    map.put("feature", new FloatArrayTypeProvider(data.vector));
    return map;
  }

}

class StringFloatArrayPair {
  final String id;
  final float[] vector;

  StringFloatArrayPair(String id, float[] vector) {
    this.id = id;
    this.vector = vector;
  }
}