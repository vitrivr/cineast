package org.vitrivr.cineast.standalone.evaluation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents a ground truth data collection that assigns IDs of test objects to arbitrary classes and
 * vice versa. This class can be constructed from a simple JSON file.
 *
 */
public class Groundtruth {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Maps class labels to document IDs. */
    private HashMap<String,Set<String>> map;

    /** Maps documentIDs class labels. */
    private HashMap<String,String> inverseMap;

    /**
     * Constructor for Groundtruth from a file.
     *
     * @param path Path to file from which to read the ground truth.
     */
    public Groundtruth(Path path) throws EvaluationException {
        this.map = new HashMap<>();
        this.inverseMap = new HashMap<>();
        try {
            JsonNode node = MAPPER.readTree(path.toFile());
            if (node == null) {
              throw new EvaluationException("Could not read the ground truth file from the specified path '%s'. File seems to be empty.");
            }
            this.readFromJsonNode(node);
        } catch (IOException e) {
            throw new EvaluationException(String.format("Could not read the ground truth file from the specified path '%s' due to an IOException.", path.toString()));
        }
    }

    /**
     * Constructor for Groundtruth from a JsonNode.
     *
     * @param node JsonNode from which to read the ground truth.
     */
    public Groundtruth (JsonNode node) throws EvaluationException {
        this.map = new HashMap<>();
        this.inverseMap = new HashMap<>();
        this.readFromJsonNode(node);
    }

    /**
     * Returns the class label for the provided ID or an empty Optional, if no class
     * has been defined for the provided ID.
     *
     * @return String class label for the provided ID.
     */
    public final Optional<String> classForDocId(String id) {
        return Optional.ofNullable(this.inverseMap.get(id));
    }

    /**
     * Returns the IDs saved under the provided class according to the ground truth. The return set
     * is empty if the class does not exist.
     *
     * @return Set of IDs for the provided class.
     */
    public final Set<String> docIdsForClass(String c) {
        return Collections.unmodifiableSet(this.map.getOrDefault(c, new HashSet<>(0)));
    }

    /**
     * Returns the number of classes registered in the Groundtruth.
     *
     * @return Number of classes.
     */
    public final int numberOfClasses() {
        return this.map.size();
    }


    /**
     * Returns the number of relevant documents for the provided class
     * label.
     *
     * @param cl Class label.
     * @return Number of relevant documents in that class.
     */
    public final int numberOfRelevant(String cl) {
        if (this.map.containsKey(cl)) {
            return this.map.get(cl).size();
        } else {
            return 0;
        }
    }

    /**
     * This method reads ground truth data from a JSON file.
     *
     * @param node JsonNode from which to read the ground truth.
     */
    private void readFromJsonNode(JsonNode node) throws EvaluationException {
        Iterator<String> classes = node.fieldNames();
        while (classes.hasNext()) {
            String cl = classes.next();
            JsonNode ids = node.get(cl);

            if (ids == null) {
              continue;
            }

            /* Add class label to map if it's not already there. */
            if (!this.map.containsKey(cl)) {
              this.map.put(cl, new HashSet<>());
            }

            if (ids.isArray()) {
                for (JsonNode item : ids) {
                    if (this.inverseMap.containsKey(cl)) {
                        throw new EvaluationException("Could not create ground truth due to a structural error: An ID must not belong to more than one class.");
                    }
                    this.map.get(cl).add(item.textValue());
                    this.inverseMap.put(item.textValue(), cl);
                }
            }
        }
    }
}
