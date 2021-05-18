package org.vitrivr.cineast.standalone.importer.vbs2019;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.importer.Importer;

public class TagImporter implements Importer<String[]> {

  private static final Logger LOGGER = LogManager.getLogger();
  private final String[] columnNames;
  private final List<String> lines;
  private int index;

  public TagImporter(Path input, String... columnNames) throws IOException {
    this.columnNames = columnNames;
    lines = FileUtils.readLines(input.toFile(), Charset.defaultCharset());
    index = 0;
  }

  private synchronized Optional<String[]> nextPair() {
    if (index < (lines.size() - 1)) {
      String line = lines.get(index);
      String[] split = line.split("\t");
      index++;
      return Optional.of(split);
    }
    return Optional.empty();
  }

  /**
   * @return Pair mapping a segmentID to a List of Descriptions
   */
  @Override
  public String[] readNext() {
    try {
      return nextPair().orElse(null);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(String[] data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(data.length);
    for (int i = 0; i < data.length; i++) {
      if (columnNames[i] == null) {
        continue;
      }
      if (columnNames[i].equals(TagReader.TAG_NAME_COLUMNNAME) || columnNames[i].equals(TagReader.TAG_DESCRIPTION_COLUMNNAME)) {
        if (data[i].contains("@")) {
          map.put(columnNames[i], PrimitiveTypeProvider.fromObject(data[i].substring(0, data[i].indexOf("@"))));
          continue;
        }
      }
      if (columnNames[i].equals("id")) {
        if (!data[i].startsWith("v_") && !data[i].startsWith("Q")) {
          map.put(columnNames[i], PrimitiveTypeProvider.fromObject("v_" + data[i]));
          continue;
        }
        //id can also be Q...
      }
      map.put(columnNames[i], PrimitiveTypeProvider.fromObject(data[i]));
    }
    if (Arrays.asList(columnNames).contains(TagReader.TAG_DESCRIPTION_COLUMNNAME) && !map.containsKey(TagReader.TAG_DESCRIPTION_COLUMNNAME)) {
      map.put(TagReader.TAG_DESCRIPTION_COLUMNNAME, PrimitiveTypeProvider.fromObject(""));
    }
    // since there is no score provided...
    if (data.length < columnNames.length) {
      for (int i = data.length; i < columnNames.length; i++) {
        if (columnNames[i] == null) {
          continue;
        }
        if (columnNames[i].equals("score")) {
          map.put(columnNames[i], PrimitiveTypeProvider.fromObject(1));
        }
      }
    }
    return map;
  }
}
