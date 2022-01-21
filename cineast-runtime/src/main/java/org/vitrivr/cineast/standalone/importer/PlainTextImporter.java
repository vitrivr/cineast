package org.vitrivr.cineast.standalone.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

public class PlainTextImporter implements Importer<Pair<String, String>> {

  /**
   * Iterator over the files that should be imported.
   */
  private final Iterator<Path> files;

  /**
   * Constructor for {@link PlainTextImporter}.
   *
   * @param input Path to the input file or folder.
   */
  public PlainTextImporter(Path input) throws IOException {
    this.files = Files.walk(input, 2).filter(s -> s.toString().endsWith(".txt")).iterator();
  }

  /**
   * Returns the ID and the content of the next text file.
   *
   * @return Pair mapping an ID (from the filename) to the content of the file (text).
   */
  @Override
  public Pair<String, String> readNext() {
    try {
      if (this.files.hasNext()) {
        final Path path = this.files.next();
        final String segmentId = path.getFileName().toString().replace(".txt", "");
        final String text = new String(Files.readAllBytes(path));
        return new Pair<>(segmentId, text);
      } else {
        return null;
      }
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Converts a mapping from ID to text content to a HashMap representation that can be used by the {@link Copier} class
   *
   * @param data Pair mapping an ID to a text.
   * @return HashMap containing the data.
   */
  @Override
  public Map<String, PrimitiveTypeProvider> convert(Pair<String, String> data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    map.put("id", PrimitiveTypeProvider.fromObject(data.first));
    map.put("feature", PrimitiveTypeProvider.fromObject(data.second));
    return map;
  }
}
