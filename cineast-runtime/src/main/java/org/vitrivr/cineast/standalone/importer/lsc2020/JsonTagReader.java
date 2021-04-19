package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;


/**
 * Reader for the tag format of tags, published by the MySceal Team in 2021.
 *
 * Requires a JSON file (published in 2021 at http://lsc.dcu.ie/resources/microsoft_tags.json
 *
 * Format:
 *
 * <code>
 *   {
 *     "item1":{
 *       "tag1": score,
 *       "tag2": score
 *     },
 *     "item2":{
 *       "tag1":score,
 *       "tag2":score
 *     }
 *   }
 * </code>
 *
 * {@code item} is formatted as filename, which {@link LSCUtilities#pathToSegmentId(String)} translates into vitrivr id
 */
public class JsonTagReader {


  private static final Logger LOGGER = LogManager.getLogger(JsonTagReader.class);

  private final Path file;

  private final HashSet<String> uniqueTags = new HashSet<>();
  private final HashMap<String, List<Pair<String, Float>>> tagEntries = new HashMap<>();

  public JsonTagReader(Path file) throws IOException {
    this.file = file;
    readEntries();
  }

  private void readEntries() throws IOException {
    LOGGER.info("Reading JSON File. This may take a while...");
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(file.toFile());
    LOGGER.info("Completed reading JSON file. Processing now...");

    rootNode.fields().forEachRemaining(entry -> {
      String segmentId = LSCUtilities.pathToSegmentId(entry.getKey());
      JsonNode tags = entry.getValue();
      List<Pair<String, Float>> segmentTags = new ArrayList<>();
      if(!tags.isEmpty()){
        tags.fields().forEachRemaining(tagEntry -> {
          if(tagEntry.getValue().isFloatingPointNumber()){
            segmentTags.add(new Pair<>(tagEntry.getKey(), tagEntry.getValue().floatValue()));
          }
          uniqueTags.add(tagEntry.getKey());
        });
      }
      tagEntries.put(segmentId, segmentTags);
    });

    LOGGER.info("Successfully processed all tags");
  }

  public HashSet<String> getUniqueTags(){
    return uniqueTags;
  }

  public HashMap<String, List<Pair<String, Float>>> getTagScoreMap(){
    return tagEntries;
  }


}
