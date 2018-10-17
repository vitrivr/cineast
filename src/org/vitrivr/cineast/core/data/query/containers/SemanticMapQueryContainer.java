package org.vitrivr.cineast.core.data.query.containers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vitrivr.cineast.core.data.SemanticMap;
import org.vitrivr.cineast.core.util.web.DataURLParser;
import org.vitrivr.cineast.core.util.web.ImageParser;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Optional;

public class SemanticMapQueryContainer extends QueryContainer {

    private final SemanticMap map;

    public SemanticMapQueryContainer(String data){
        this(DataURLParser.dataURLtoJsonNode(data).orElseThrow(() -> new IllegalArgumentException("Failed to parse the provided semantic map data.")));
    }

    public SemanticMapQueryContainer(JsonNode jsonNode){
        final BufferedImage image = ImageParser.dataURLtoBufferedImage(jsonNode.get("image").asText());
        final Map<String, String> classes = (new ObjectMapper()).convertValue(jsonNode.get("classes"), Map.class);
        this.map = new SemanticMap(image, classes);
    }

    @Override
    public Optional<SemanticMap> getSemanticMap() {
        return Optional.of(this.map);
    }
}
