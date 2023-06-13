package org.vitrivr.cineast.core.extraction.idgenerator;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.json.JsonReader;

/**
 * Generates objectIds from a provided list of objectId's. Those ID's are either assigned in sequence OR mapped based on the filename. It is up to the author of such a list to ensure that there are enough ID's for the files in a run AND that those ID's are unique.
 */
public class JSONProvidedObjectIdGenerator implements ObjectIdGenerator {

  /**
   * Property-name for a custom start value (can be set in the configuration).
   */
  private static final String PROPERTY_NAME_SOURCE = "source";
  /**
   * Property-name for a custom format (can be set  in the configuration).
   */
  private static final String PROPERTY_NAME_ASSIGNMENT = "assignment";
  /**
   * Map that maps filenames to ID's. Only used in MAP mode.
   */
  private HashMap<String, Object> pathIdMap;
  /**
   * List of ID's. Only used in CONTINUOUS mode.
   */
  private LinkedList<String> idList;
  /**
   * The mode of assignment for ID's.
   */
  private final AssignmentMode mode;

  /**
   * Constructor for {@link JSONProvidedObjectIdGenerator}.
   *
   * @param properties HashMap of named parameters. The values 'source' and 'assignment' are supported parameter keys.
   */
  @SuppressWarnings("unchecked")
  public JSONProvidedObjectIdGenerator(Map<String, String> properties) {
    String assignment = properties.getOrDefault(PROPERTY_NAME_ASSIGNMENT, AssignmentMode.MAP.name());
    this.mode = AssignmentMode.valueOf(assignment.toUpperCase());
    final String source = properties.get(PROPERTY_NAME_SOURCE);
    final JsonReader reader = new JacksonJsonProvider();
    switch (mode) {
      case MAP -> {
        this.pathIdMap = reader.toObject(new File(source), HashMap.class);
        this.idList = null;
      }
      case CONTINUOUS -> {
        this.idList = reader.toObject(new File(source), LinkedList.class);
        this.pathIdMap = null;
      }
    }
  }

  /**
   * Generates the next objectId and returns it as a string. That objectId not have a MediaType prefix!
   *
   * @param path Path to the file for which an ID should be generated.
   * @param type MediaType of the file for which an ID should be generated.
   * @return Next ID in the sequence.
   */
  @Override
  public Optional<String> next(Path path, MediaType type) {
    switch (mode) {
      case MAP -> {
        return Optional.of(this.pathIdMap.get(path.getFileName().toString()).toString());
      }
      case CONTINUOUS -> {
        String id = this.idList.poll();
        if (id == null) {
          return Optional.empty();
        }
        return Optional.of(id);
      }
      default -> throw new IllegalArgumentException("Mode " + mode + "not implemented");
    }
  }

  /**
   * Defines the assignment-modes for provided objectIds.
   */
  private enum AssignmentMode {
    MAP, /* Expects a JSON object with {<filename>:<id>} pairs as entries. Each path is mapped to its ID. */
    CONTINUOUS /* Expects a JSON array with one ID per row. The ID's are assigned in a continuous fashion. */
  }
}
