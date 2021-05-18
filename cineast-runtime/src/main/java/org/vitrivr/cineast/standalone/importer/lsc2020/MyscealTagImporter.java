package org.vitrivr.cineast.standalone.importer.lsc2020;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.standalone.config.Config;

public class MyscealTagImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

  private static final Logger LOGGER = LogManager.getLogger(MyscealTagImporter.class);

  private final Path file;

  private volatile Iterator<String> uniqueTagIterator;
  private volatile Iterator<Entry<String, List<Pair<String, Float>>>> tagScoreIterator;
  private volatile String currentSegmentId;
  private volatile Iterator<Pair<String, Float>> currentIterator;

  private final boolean tagLookup;

  private final TagReader tagReader;

  /**
   * @param file      - The JSON file with the tags
   * @param tagLookup - Whether the import is for the taglookup or actual tags with scores (true for
   *                  lookup)
   */
  public MyscealTagImporter(final Path file, boolean tagLookup) {
    this.file = file;
    this.tagLookup = tagLookup;
    try {
      final JsonTagReader reader = new JsonTagReader(file);
      uniqueTagIterator = reader.getUniqueTags().stream().iterator();
      tagScoreIterator = reader.getTagScoreMap().entrySet().iterator();
      if (tagLookup) {
        tagReader = new TagReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
      }else{
        tagReader = null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not parse json file", e);
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> readNext() {
    if(this.tagLookup){
      return readNextTagLookup();
    }else {
      return readNextTagScore();
    }
  }

  /**
   * Returns the next tag score element.
   * In other words, this returns the next triple segmentId, tagId, score als long as there are these triples.
   * Those triples are constructed by first getting the current segmentId and then iterating over this segment's tags until they are all processed
   * @return
   */
  private Map<String, PrimitiveTypeProvider> readNextTagScore() {
    do{
      if(currentSegmentId == null && tagScoreIterator.hasNext()){
        /* Get current segment id and iterator */
        final Map.Entry<String, List<Pair<String,Float>>> entry = tagScoreIterator.next();
        currentSegmentId = entry.getKey();
        currentIterator = entry.getValue().iterator();
      }
      if(currentIterator.hasNext()){
        /* Commit current segment tag with score */
        final Pair<String, Float> segmentTag = currentIterator.next();
        final Map<String, PrimitiveTypeProvider> map = new HashMap<>();
        map.put("id", PrimitiveTypeProvider.fromObject(currentSegmentId));
        map.put("tagid", PrimitiveTypeProvider.fromObject(segmentTag.first));
        map.put("score", PrimitiveTypeProvider.fromObject(segmentTag.second));
        return map;
      }else{
        /* Reset current iterator & segmentId */
        currentIterator = null;
        currentSegmentId = null;
      }
    }while(currentIterator == null && tagScoreIterator.hasNext());
    return null;
  }

  private Map<String, PrimitiveTypeProvider> readNextTagLookup() {
    if (this.uniqueTagIterator.hasNext()) {
      final String next = this.uniqueTagIterator.next();
      // Only add new tags to the system
      if (tagReader.getTagById(next) != null) {
        final Map<String, PrimitiveTypeProvider> map = new HashMap<>();
        map.put(TagReader.TAG_ID_COLUMNNAME, PrimitiveTypeProvider.fromObject(next));
        map.put(TagReader.TAG_NAME_COLUMNNAME, PrimitiveTypeProvider.fromObject(next));
        map.put(TagReader.TAG_DESCRIPTION_COLUMNNAME, PrimitiveTypeProvider
            .fromObject(next)); // LSC Context: No description available. Use label instead
        return map;
      }
    }
    return null;
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Map<String, PrimitiveTypeProvider> data) {
    return data;
  }

}
