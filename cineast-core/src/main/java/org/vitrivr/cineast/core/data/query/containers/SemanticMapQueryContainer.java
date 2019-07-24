package org.vitrivr.cineast.core.data.query.containers;

import com.fasterxml.jackson.databind.JsonNode;

import org.vitrivr.cineast.core.data.SemanticMap;
import org.vitrivr.cineast.core.util.web.DataURLParser;
import org.vitrivr.cineast.core.util.web.ImageParser;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link QueryContainer} for queries using semantic maps. The class expects a JSON of the following form:
 *
 * {
 *     image: <Base 64 encoded image>,
 *     map: List of objects mapping the class to the color (as used in the image.
 * }
 */
public class SemanticMapQueryContainer extends QueryContainer {

    /** Field name of the image portion of the JSON. */
    private static final String IMAGE_FIELD_NAME = "image";

    /** Field name of the map portion of the JSON. */
    private static final String MAP_FIELD_NAME = "map";

    /** The {@link SemanticMap} that is used internally. */
    private final SemanticMap map;

    /**
     * Constructor for {@link SemanticMapQueryContainer}
     *
     * @param data Base64 encoded representation of the JSON.
     */
    public SemanticMapQueryContainer(String data){
        this(DataURLParser.dataURLtoJsonNode(data).orElseThrow(() -> new IllegalArgumentException("Failed to parse the provided semantic map data.")));
    }

    /**
     * Constructor for {@link SemanticMapQueryContainer}
     *
     * @param jsonNode JsonObject as expected by {@link SemanticMapQueryContainer}
     */
    public SemanticMapQueryContainer(JsonNode jsonNode){
        if (!jsonNode.has(IMAGE_FIELD_NAME)) throw new IllegalArgumentException("The provided data structure does not contain the required field 'image' (semantic map).");
        if (!jsonNode.has(MAP_FIELD_NAME)) throw new IllegalArgumentException("The provided data structure does not contain the required field 'map' (category to color map).");

        final BufferedImage image = ImageParser.dataURLtoBufferedImage(jsonNode.get(IMAGE_FIELD_NAME).asText());
        final Map<String, String> classes = new LinkedHashMap<>();

        if (jsonNode.get(MAP_FIELD_NAME).isArray()) {
            for (JsonNode node : jsonNode.get(MAP_FIELD_NAME)) {
                if (node.isObject()) {
                    node.fieldNames().forEachRemaining(f -> {
                        classes.put(f, node.get(f).asText());
                    });
                }
            }
        }
        this.map = new SemanticMap(image, classes);
    }

    /**
     * Getter for {@link SemanticMapQueryContainer#map}.
     *
     * @return {@link SemanticMap}
     */
    @Override
    public Optional<SemanticMap> getSemanticMap() {
        return Optional.of(this.map);
    }
}
