package org.vitrivr.cineast.core.idgenerator;

import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.json.JsonReader;

import java.io.File;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Generates objectIds from a provided list of objectId's. Those ID's are either assigned in sequence OR mapped
 * based on the filename. It is up to the author of such a list to ensure that there are enough ID's for the
 * files in a run AND that those ID's are unique.
 *
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public class JSONProvidedObjectIdGenerator implements ObjectIdGenerator {

    /**
     * Defines the assignment-modes for provided objectIds.
     */
    private enum AssignmentMode {
        MAP, /* Expects a JSON object with {<filename>:<id>} pairs as entries. Each path is mapped to its ID. */
        CONTINUOUS /* Expects a JSON array with one ID per row. The ID's are assigned in a continuous fashion. */
    }

    /** Property-name for a custom start value (can be set in the configuration). */
    private static final String PROPERTY_NAME_SOURCE = "source";

    /** Property-name for a custom format (can be set  in the configuration). */
    private static final String PROPERTY_NAME_ASSIGNMENT = "assignment";

    /** Map that maps filenames to ID's. Only used in MAP mode. */
    private HashMap<String, String> pathIdMap;

    /** List of ID's. Only used in CONTINUOUS mode. */
    private LinkedList<String> idList;

    /** The mode of assignment for ID's. */
    private AssignmentMode mode = AssignmentMode.MAP;

    /** JSONReader used to read the file containing the ID's. */
    private JsonReader reader = new JacksonJsonProvider();

    /**
     * Can be used to initialize a particular ObjectIdGenerator instance by passing
     * a HashMap of named parameters.
     *
     * 'source' and 'assignment' are supported parameters.
     *
     * @param properties HashMap of named parameters.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void init(HashMap<String, String> properties) {
        String assignment = properties.get(PROPERTY_NAME_ASSIGNMENT);
        if (assignment != null) this.mode = AssignmentMode.valueOf(assignment.toUpperCase());

        String source = properties.get(PROPERTY_NAME_SOURCE);
        if (mode == AssignmentMode.MAP) {
            this.pathIdMap = this.reader.toObject(new File(source), HashMap.class);
            this.idList = null;
        } else {
           this.idList = this.reader.toObject(new File(source), LinkedList.class);
           this.pathIdMap = null;
        }
    }

    /**
     * Generates the next objectId and returns it as a string. That objectId not have a
     * MediaType prefix!
     *
     * @param path Path to the file for which an ID should be generated.
     * @param type MediaType of the file for which an ID should be generated.
     * @return Next ID in the sequence.
     */
    @Override
    public String next(Path path, MediaType type) {
        if (mode == AssignmentMode.MAP) {
            return this.pathIdMap.get(path.getFileName().toString());
        } else {
            return this.idList.poll();
        }
    }
}
